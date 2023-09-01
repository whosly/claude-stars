package com.yueny.study.netty.allocator;

import com.yueny.study.netty.buffer.WrappedAutoFlushByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * 达到 MaxCapacity 后不在扩容，直接报错
 *
 * @author fengyang
 * @date 2023/8/10 下午4:13
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ByteBufWriteMaxCapacityTest
{
    private static Logger log = LoggerFactory.getLogger(ByteBufWriteMaxCapacityTest.class);

    private DirectByteBufPooledAllocator pool;
    private Random random = new Random();

    @Before
    public void before() {
        this.pool = ByteBufPoolManager
                .getInstance()
                .createDirectByteBufPooled();

        int initialCapacity = pool.getConfigInitialCapacity();
        log.info("initialCapacity:{}.", initialCapacity);

        Assert.assertEquals(pool.cacheSize(), 320);
    }

    @Test
    public void testCapacityNotFull()
    {
        WrappedAutoFlushByteBuf wrappedByteBuf = pool.allocByteBuf(1024, 2048L);

        for (int i = 0; i < 1024; i++) {
            wrappedByteBuf.writeByte(i);
        }
        Assert.assertTrue(wrappedByteBuf.capacity() == 1024);
        Assert.assertTrue(wrappedByteBuf.maxCapacity() == 2048);
        Assert.assertEquals(wrappedByteBuf.writerIndex(), 1024);
        Assert.assertEquals(wrappedByteBuf.readerIndex(), 0);

        wrappedByteBuf.readByte();

        for (int i = 0; i < 1024; i++) {
            wrappedByteBuf.writeByte(i);
        }
        Assert.assertTrue(wrappedByteBuf.capacity() == 2048);
        Assert.assertTrue(wrappedByteBuf.maxCapacity() == 2048);
        Assert.assertEquals(wrappedByteBuf.writerIndex(), 2048);
        Assert.assertEquals(wrappedByteBuf.readerIndex(), 1);

//        // 满了不能继续写数据
//        for (int i = 0; i < 5; i++) {
//            byteBuf.writeByte(i);
//        }

        ReferenceCountUtil.release(wrappedByteBuf);
        Assert.assertTrue(wrappedByteBuf.capacity() == 2048);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCapacityFull()
    {
        WrappedAutoFlushByteBuf wrappedByteBuf = pool.allocByteBuf(1024, 2048L);

        for (int i = 0; i < 1024; i++) {
            wrappedByteBuf.writeByte(i);
        }
        Assert.assertTrue(wrappedByteBuf.capacity() == 1024);
        Assert.assertTrue(wrappedByteBuf.maxCapacity() == 2048);
        Assert.assertEquals(wrappedByteBuf.writerIndex(), 1024);
        Assert.assertEquals(wrappedByteBuf.readerIndex(), 0);

        wrappedByteBuf.readByte();

        for (int i = 0; i < 1024; i++) {
            wrappedByteBuf.writeByte(i);
        }
        Assert.assertTrue(wrappedByteBuf.capacity() == 2048);
        Assert.assertTrue(wrappedByteBuf.maxCapacity() == 2048);
        Assert.assertEquals(wrappedByteBuf.writerIndex(), 2048);
        Assert.assertEquals(wrappedByteBuf.readerIndex(), 1);

        // 满了不能继续写数据
        for (int i = 0; i < 5; i++) {
            wrappedByteBuf.writeByte(i);
        }
    }

    @Test
    public void testCapacityFullAndMsg()
    {
        WrappedAutoFlushByteBuf wrappedByteBuf = pool.allocByteBuf(1024, 2048L);

        for (int i = 0; i < 1024; i++) {
            wrappedByteBuf.writeByte(i);
        }
        Assert.assertTrue(wrappedByteBuf.capacity() == 1024);
        Assert.assertTrue(wrappedByteBuf.maxCapacity() == 2048);
        Assert.assertEquals(wrappedByteBuf.writerIndex(), 1024);
        Assert.assertEquals(wrappedByteBuf.readerIndex(), 0);

        wrappedByteBuf.readByte();

        for (int i = 0; i < 1024; i++) {
            wrappedByteBuf.writeByte(i);
        }
        Assert.assertTrue(wrappedByteBuf.capacity() == 2048);
        Assert.assertTrue(wrappedByteBuf.maxCapacity() == 2048);
        Assert.assertEquals(wrappedByteBuf.writerIndex(), 2048);
        Assert.assertEquals(wrappedByteBuf.readerIndex(), 1);

        // 满了不能继续写数据
        try {
            for (int i = 0; i < 5; i++) {
                wrappedByteBuf.writeByte(i);
            }
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(e.getMessage(),
                    "writerIndex(2048) + minWritableBytes(1) exceeds maxCapacity(2048): PooledUnsafeDirectByteBuf(ridx: 1, widx: 2048, cap: 2048/2048)");
        }
    }

}
