package com.yueny.study.netty.capacity.allocator;

import com.yueny.study.netty.capacity.buffer.WrappedAutoFlushByteBuf;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ByteBuf 做 release  的 Case
 *
 * @author fengyang
 * @date 2023/8/14 下午3:08
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ByteBufPoolReleaseTest
{
    private static Logger log = LoggerFactory.getLogger(ByteBufPoolReleaseTest.class);

    private static boolean isRelease = true;

    @Test
    public void testRelease() {
        System.setProperty("io.netty.maxDirectMemory", "2147483648");

        DirectByteBufPooledAllocator pool = ByteBufPoolManager
                .getInstance()
                .createDirectByteBufPooled();

        Random random = new Random();

        int initialCapacity = pool.getConfigInitialCapacity();
        log.info("initialCapacity:{}.", initialCapacity);

        Assert.assertEquals(pool.cacheSize(), 320);

        WrappedAutoFlushByteBuf wByteBuf = pool.allocByteBuf();
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigInitialCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), 0);
        Assert.assertTrue(wByteBuf.maxCapacity() == pool.getConfigMaxCapacity());

        for (int i = 0; i < 60; i++) {
            wByteBuf.writeByte(i);
        }
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigInitialCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), 60);

        long max = pool.getConfigMaxCapacity() - 60 - 10;
        for (int i = 0; i < max; i++) {
            wByteBuf.writeByte(i);
        }
        // 存在扩容
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigMaxCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), pool.getConfigMaxCapacity() -10);

        if(isRelease){
            wByteBuf.release();
        }

        // 500 多线程同时申请 buffer.
        ExecutorService executorService = new ThreadPoolExecutor(16,
                16,
                1,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        int size = 500;
        CountDownLatch countDownLatch = new CountDownLatch(size);

        /**
         * used DirectMemory：
         *
         * expect:
         *  10(direct arena) * 4 KB = 40KB
         *
         * actual:
         *  500 * 4KB = 2000Kb = 2MB
         */
        for (int i = 0; i < size; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    final WrappedAutoFlushByteBuf wrappedByteBuf = pool.allocByteBuf();

                    // 边写边读
                    Future futureWrite = CompletableFuture.runAsync(() -> {
                        long max_ = pool.getConfigMaxCapacity();
                        for (int j = 0; j < max_; j++) {
                            wrappedByteBuf.writeByte(j);
                        }
                    });
                    futureWrite.get();

                    Future futureRead = CompletableFuture.runAsync(() -> {
                        while (wrappedByteBuf.readableBytes() > 0) {
                            wrappedByteBuf.readByte();
                        }
                    });
                    futureRead.get();

                    PooledByteBufAllocatorMetric metric = (PooledByteBufAllocatorMetric) pool.allocatorMetric();
                    int numDirectArenas = metric.numDirectArenas();
                    log.info("allocByteBuf {}, " +
                                    "buffer capacity:{}, readerIndex:{}, writerIndex:{}, " +
                                    " numDirectArenas:{}, numThreadLocalCaches:{}, usedDirectMemory: {}.",
                            finalI,
                            wrappedByteBuf.capacity(), wrappedByteBuf.readerIndex(), wrappedByteBuf.writerIndex(),
                            numDirectArenas, metric.numThreadLocalCaches(),
                            BytesUtil.byteToM(metric.usedDirectMemory()));

                    if(isRelease){
                        // 必须要 release， 否则堆外内存不会被释放！！！
                        wrappedByteBuf.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int loopC = 10;
        log.info("loopC， times:{}.", loopC);
        while(loopC >= 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            PooledByteBufAllocatorMetric metric = (PooledByteBufAllocatorMetric) pool.allocatorMetric();
            int numDirectArenas = metric.numDirectArenas();
            log.info("allocByteBuf numDirectArenas:{}, numThreadLocalCaches:{}, " +
                            "usedDirectMemory: {}.",
                    numDirectArenas, metric.numThreadLocalCaches(),
                    BytesUtil.byteToM(metric.usedDirectMemory()));

            loopC--;
        }

        log.info("关闭线程池...");
        // 关闭线程池
        executorService.shutdownNow();
        Assert.assertTrue(executorService.isShutdown());

        log.info("执行结束!");
    }
}
