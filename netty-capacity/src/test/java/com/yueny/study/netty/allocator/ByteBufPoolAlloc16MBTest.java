package com.yueny.study.netty.allocator;

import com.yueny.study.netty.buffer.WrappedAutoFlushByteBuf;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import org.junit.Assert;
import org.junit.Before;
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
 * 分配16MB 对象的 case
 *
 * @author fengyang
 * @date 2023/8/10 上午10:48
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ByteBufPoolAlloc16MBTest
{
    private static Logger log = LoggerFactory.getLogger(ByteBufPoolAlloc16MBTest.class);

    private ByteBufPoolConfig config;

    @Before
    public void before() {
        this.config = ByteBufPoolConfig.builder()
                .maxCapacity(Allocators._1MB * 16L)
                .maxDirectMemory(DirectByteBufPooledAllocator.DEFAULT_MAX_ALLOCATOR_MEM)
                .build();
    }

    @Test
    public void testA()
    {
//        System.setProperty("io.netty.maxDirectMemory", "2147483648");

        DirectByteBufPooledAllocator pool = ByteBufPoolManager
                .getInstance()
                .createDirectByteBufPooled();

        Random random = new Random();

        int initialCapacity = pool.getConfigInitialCapacity();
        log.info("initialCapacity:{}.", initialCapacity);

        Assert.assertEquals(pool.cacheSize(), 320);

        WrappedAutoFlushByteBuf wByteBuf = pool.allocByteBuf(this.config.getMaxCapacity());
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigInitialCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), 0);
        Assert.assertTrue(wByteBuf.maxCapacity() == this.config.getMaxCapacity());

        for (int i = 0; i < 60; i++) {
            wByteBuf.writeByte(i);
        }
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigInitialCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), 60);

        long max = this.config.getMaxCapacity() - 60 - 10;
        for (int i = 0; i < max; i++) {
            wByteBuf.writeByte(i);
        }
        // 存在扩容
        Assert.assertTrue(wByteBuf.capacity() == this.config.getMaxCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), this.config.getMaxCapacity() -10);

        // 500 多线程同时申请 buffer.
        ExecutorService executorService = new ThreadPoolExecutor(16,
                16,
                1,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        int size = 1500;
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
                    final WrappedAutoFlushByteBuf wrappedByteBuf = pool.allocByteBuf(
                            this.config.getMaxCapacity()
                    );

                    // 边写边读
                    Future futureWrite = CompletableFuture.runAsync(() -> {
                        long max_ = this.config.getMaxCapacity();
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

                    // 16MB 的 byteBuf release
                    wrappedByteBuf.release();
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
