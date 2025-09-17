package com.yueny.stars.netty.capacity.allocator;

import com.yueny.stars.netty.capacity.buffer.WrappedAutoFlushByteBuf;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fengyang
 * @date 2023/8/15 下午4:41
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ByteBufPoolToNioTest
{
    private static Logger log = LoggerFactory.getLogger(ByteBufPoolReleaseTest.class);

    @Test
    public void testRelease() {
        DirectByteBufPooledAllocator pool = ByteBufPoolManager
                .getInstance()
                .createDirectByteBufPooled();

        int initialCapacity = pool.getConfigInitialCapacity();
        log.info("initialCapacity:{}.", initialCapacity);

        Assert.assertEquals(pool.cacheSize(), 320);

        WrappedAutoFlushByteBuf wByteBuf = pool.allocByteBuf();
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigInitialCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), 0);
        Assert.assertTrue(wByteBuf.maxCapacity() == pool.getConfigMaxCapacity());

        ByteBuffer nioBuf = wByteBuf.nioBuffer(0, wByteBuf.capacity());
        Assert.assertTrue(nioBuf.isDirect());

        for (int i = 0; i < 60; i++) {
            nioBuf.putInt(i);
        }
        Assert.assertTrue(wByteBuf.capacity() == pool.getConfigInitialCapacity());
        Assert.assertEquals(wByteBuf.writerIndex(), 0);
        Assert.assertEquals(nioBuf.position(), 60 * 4);

        long max = nioBuf.capacity() / 4 - 240;
        for (int i = 0; i < max; i++) {
            nioBuf.putInt(i);
        }
        // nio 不存在扩容
        Assert.assertEquals(nioBuf.position(), max * 4 + 60 * 4);
//        wByteBuf.release();

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
                    final WrappedAutoFlushByteBuf wrappedByteBuf = pool.allocByteBuf(pool.getConfigMaxCapacity().intValue(), pool.getConfigMaxCapacity());
                    final ByteBuffer nio = wrappedByteBuf.nioBuffer(0, wByteBuf.maxCapacity());

                    // 边写边读
                    Future futureWrite = CompletableFuture.runAsync(() -> {
                        long max_ = nio.capacity() / 4;
                        for (int j = 0; j < max_; j++) {
                            nio.putInt(j);
                        }
                    });
                    futureWrite.get();

                    Future futureRead = CompletableFuture.runAsync(() -> {
                        while (nio.remaining() > 0) {
                            nio.getInt();
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

//                    // 必须要 release， 否则堆外内存不会被释放！！！
                    wrappedByteBuf.release();
                    // flip: 切换读取模式，position初始为0，limit初始为可读取数据得最大下标
                    // byteBuffer.flip(); //切换读取模式，position初始为0，limit初始为可读取数据得最大下标
                    //clear: 在逻辑上清空ByteBuffer里的数据，实际上不清空数据
                    //会触发的动作：
                    //  将limit设置为capacity
                    //  position指向起始位置0
                    //提示：实际上数据并未清理，只是下次是从0的位置开始写入数据，效果上像是数据清空了。
                    //提示：如果ByteBuffer中的数据并未完全读完，调用这个方法将忽略那些未读取的数据。
//                    nio.clear();
//                    // 重复读取一次， 只是将position移到第一位置
//                    nio.rewind();
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
