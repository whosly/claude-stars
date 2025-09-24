package com.whosly.stars.netty.buffer.buf;

import com.whosly.stars.netty.buffer.allocator.ByteBufAllocatorFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-08-20 15:51:45
 * @description
 */
public class ByteBufExampleTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testCreatedByDefaultAllocator() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.defaultAllocator();

        // 返回一个基于堆外内存存储的ByteBuf
        ByteBuf buf = alloc.buffer(256, Integer.MAX_VALUE);
        assertTrue(buf.isDirect());

        //2、返回一个基于堆内存存储的 ByteBuf 实例
        ByteBuf byteBuf1 = alloc.heapBuffer(256);
        assertFalse(byteBuf1.isDirect());
        //检查该ByteBuf的引用计数
        assertEquals(1, byteBuf1.refCnt());
        //将ByteBuf的引用计数设为0并释放
        byteBuf1.release();
        assertEquals(0, byteBuf1.refCnt());
    }

    @Test
    public void testCreatedByirect() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.defaultAllocator();

        // 返回一个基于直接内存存储的 ByteBuf
        ByteBuf byteBuf = alloc.directBuffer();
        assertTrue(byteBuf.isDirect());
    }

    @Test
    public void testCreatedCompositeByteBuf() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.defaultAllocator();

        // 返回一个可以通过添加最大到指定数目的【基于堆的或者直接内存存储】的缓冲区来扩展的 CompositeByteBuf
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        ByteBuf headerBuf = Unpooled.buffer(1024);
        ByteBuf bodyBuf = Unpooled.wrappedBuffer("Hello Netty".getBytes());
        messageBuf.addComponents(headerBuf, bodyBuf);

        assertFalse(headerBuf.isDirect());
        assertFalse(bodyBuf.isDirect());
        assertFalse(messageBuf.isDirect());

        // remove the header
        messageBuf.removeComponent(0);
        for (ByteBuf buf : messageBuf) {
            System.out.println(buf.toString(Charset.forName("UTF-8")));
        }
        ByteBuf sliced = messageBuf.slice(0, 11);
        System.out.println(sliced.toString(Charset.forName("UTF-8")));
        sliced.setByte(0, (byte)'J');
        assertTrue(messageBuf.getByte(0) == sliced.getByte(0));

        CompositeByteBuf compositeByteBuf1 = alloc.compositeBuffer();
        CompositeByteBuf compositeByteBuf2 = alloc.compositeHeapBuffer(16);
        CompositeByteBuf compositeByteBuf3 = alloc.compositeDirectBuffer(16);
        assertFalse(messageBuf.isDirect());
        assertFalse(compositeByteBuf1.isDirect());
        assertFalse(compositeByteBuf2.isDirect());
        assertFalse(compositeByteBuf3.isDirect());
    }

    @Test
    public void testCreatedIOByteBuf() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.defaultAllocator();

        // 返回一个用于套接字的 I/O 操作的ByteBuf
        ByteBuf byteBuf = alloc.ioBuffer();
        assertTrue(byteBuf.isDirect());
    }

    @Test
    public void testCreatedUnpooled() {
        // 创建一个未池化的基于内存存储的ByteBuf
        ByteBuf byteBuf = Unpooled.directBuffer(256, Integer.MAX_VALUE);

        assertTrue(byteBuf.isDirect());
    }

    @Test
    public void testCreatedUnpooled1() {
        // 返回一个包装了给定数据的 ByteBuf
        ByteBuf byteBufWrapped = Unpooled.wrappedBuffer("Hello Netty".getBytes());
        assertFalse(byteBufWrapped.isDirect());

        // 返回一个复制了给定数据的 ByteBuf
        ByteBuf byteBufCopied = Unpooled.copiedBuffer("Hello Netty", CharsetUtil.UTF_8);

        assertFalse(byteBufCopied.isDirect());
    }

    @Test
    public void testCreatedByAllocator() {
        // 池化的基于堆外内存存储的
        ByteBuf buf = ByteBufExample.createdByPooledAllocator();

        assertTrue(buf.isDirect());
    }

    @Test
    public void testCreatedByUnpooled() {
        // 未池化的基于堆内存存储
        ByteBuf heapBuf = ByteBufExample.createdByUnpooled();

        assertFalse(heapBuf.isDirect());

        if (heapBuf.hasArray()) {
            byte[] array = heapBuf.array();
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            int length = heapBuf.readableBytes();

            assertEquals(offset, 0);
            assertEquals(length, 0);

            heapBuf.writeByte(55);
            heapBuf.writeByte(44);
            byte b2 = heapBuf.readByte();

            offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            length = heapBuf.readableBytes();
            assertEquals(offset, 1);
            assertEquals(length, 1);
        }
    }
}