package com.whosly.stars.netty.capacity.allocator;

import com.whosly.stars.netty.capacity.buffer.WrappedAutoFlushByteBuf;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author fengyang
 * @date 2023/8/14 下午3:08
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ByteBufPoolOneThreadTest
{
    private static Logger log = LoggerFactory.getLogger(ByteBufPoolOneThreadTest.class);

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

        // not release

        // 500 次申请 buffer.

        /**
         * used DirectMemory：
         *
         * expect:
         *  6(direct arena) * 4 KB = 40KB
         *
         * actual:
         *  500 * 0.5MB = 250MB
         */
        for (int i = 0; i < 1500; i++) {
            int finalI = i;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
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

        log.info("执行结束!");
    }
}
