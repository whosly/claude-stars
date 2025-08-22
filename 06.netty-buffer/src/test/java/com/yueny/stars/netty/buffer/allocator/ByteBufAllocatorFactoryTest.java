package com.yueny.stars.netty.buffer.allocator;

import com.yueny.stars.netty.buffer.ByteBufPositionTest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.local.LocalChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.netty.channel.DummyChannelHandlerContext.DUMMY_INSTANCE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-08-20 15:07:13
 * @description
 */
public class ByteBufAllocatorFactoryTest {

    @BeforeEach
    public void setUp() {
        //.
    }

    @Test
    public void testAllocatorByChannelContext() {
        ChannelHandlerContext channelHandlerContext = DUMMY_INSTANCE;

        ByteBufAllocator alloc = ByteBufAllocatorFactory.allocatorByChannel(channelHandlerContext);
        assertNotNull(alloc);

        ByteBuf buffer = alloc.buffer();
        assertFalse(buffer.isDirect());

        ByteBufPositionTest.readAndWrite(alloc.heapBuffer(256), 256);
    }

    @Test
    public void testAllocatorByChannel() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.allocatorByChannel(new LocalChannel());
        assertNotNull(alloc);

        ByteBuf buffer = alloc.buffer();
        assertFalse(buffer.isDirect());

        ByteBufPositionTest.readAndWrite(alloc.heapBuffer(512), 512);
    }

    @Test
    public void testDefaultAllocator() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.defaultAllocator();
        assertNotNull(alloc);

        ByteBuf buffer = alloc.buffer();
        assertTrue(buffer.isDirect());

        ByteBufPositionTest.readAndWrite(alloc.heapBuffer(16), 16);
    }

    @Test
    public void testPooledAllocator() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.pooledAllocator();
        assertNotNull(alloc);

        ByteBuf buffer = alloc.buffer();
        assertTrue(buffer.isDirect());

        ByteBufPositionTest.readAndWrite(alloc.heapBuffer(2), 2);
    }

    @Test
    public void testUnpooledAllocator() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.unpooledAllocator();
        assertNotNull(alloc);

        ByteBuf buffer = alloc.buffer();
        assertTrue(buffer.isDirect());

        ByteBufPositionTest.readAndWrite(alloc.heapBuffer(6), 6);
    }

    @Test
    public void testAllocatorByNioSocket() {
        ByteBufAllocator alloc = ByteBufAllocatorFactory.allocatorByNioSocket();
        assertNotNull(alloc);

        ByteBuf buffer = alloc.buffer();
        assertTrue(buffer.isDirect());

        ByteBufPositionTest.readAndWrite(alloc.heapBuffer(1), 1);
    }

}