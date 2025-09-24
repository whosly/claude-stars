package com.whosly.stars.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-08-20 15:46:35
 * @description
 */
public class ByteBufPositionTest {

    @BeforeEach
    public void setUp() {
        //.
    }

    @Test
    public void testByteBuf() {
        ByteBuf byteBuf = Unpooled.buffer();

        readAndWrite(byteBuf, byteBuf.capacity());
    }

    public static void readAndWrite(ByteBuf byteBuf, int initialCapacity) {
        assertEquals(initialCapacity, byteBuf.capacity());

        //检查该ByteBuf的引用计数
        int cnt = byteBuf.refCnt();
        assertEquals(1, cnt);

        byteBuf.writeByte(55);
        byteBuf.writeByte(44);
        byteBuf.writeByte(33);
        // readerIndex位置
        assertEquals(0, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(3, byteBuf.writerIndex());

        byte[] bytes = {73, 74, 61, 63};
        byteBuf.writeBytes(bytes);
        // readerIndex位置
        assertEquals(0, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(7, byteBuf.writerIndex());

        byte[] allBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(allBytes);
        // readerIndex位置
        assertEquals(7, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(7, byteBuf.writerIndex());

        byteBuf.resetReaderIndex();
        // readerIndex位置
        assertEquals(0, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(7, byteBuf.writerIndex());

        byte b0 = byteBuf.readByte();
        byte b1 = byteBuf.readByte();
        byte b2 = byteBuf.readByte();
        // readerIndex位置
        assertEquals(3, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(7, byteBuf.writerIndex());

        // 读4个，写入一个新的buffer
        ByteBuf newByteBuf= byteBuf.readBytes(4);
        byte[] dstNew = new byte[4];
        newByteBuf.readBytes(dstNew);
        // readerIndex位置
        assertEquals(4, newByteBuf.readerIndex());
        // writerIndex位置
        assertEquals(4, newByteBuf.writerIndex());

        boolean releaseNewByteBuf = newByteBuf.release();
        assertTrue(releaseNewByteBuf);

        byteBuf.resetReaderIndex();
        // readerIndex位置
        assertEquals(0, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(7, byteBuf.writerIndex());

        // read position is 0
        byteBuf.markReaderIndex();

        byte[] dst = new byte[4];
        byteBuf.readBytes(dst);
        // readerIndex位置
        assertEquals(4, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(7, byteBuf.writerIndex());

        byteBuf.writeByte(66);
        byteBuf.writeByte(88);
        // readerIndex位置
        assertEquals(4, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(9, byteBuf.writerIndex());

        // readInt() 需要连续 4 个可读字节
        byteBuf.readInt();
        // readerIndex位置
        assertEquals(8, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(9, byteBuf.writerIndex());

        // 回到标记的 0。 read position is set 0
        byteBuf.resetReaderIndex();
        // readerIndex位置
        assertEquals(0, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(9, byteBuf.writerIndex());

        byteBuf.clear();
        // readerIndex位置
        assertEquals(0, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(0, byteBuf.writerIndex());

        byte[] bytes1 = {73, 74, 61, 63};
        byteBuf.writeBytes(bytes1);
        byte b5 = byteBuf.readByte();
        // readerIndex位置
        assertEquals(1, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(4, byteBuf.writerIndex());

        //将ByteBuf的引用计数设为0并释放
        boolean releaseByteBuf = byteBuf.release();
        assertTrue(releaseByteBuf);
        // readerIndex位置
        assertEquals(1, byteBuf.readerIndex());
        // writerIndex位置
        assertEquals(4, byteBuf.writerIndex());

        assertEquals(0, byteBuf.refCnt());
    }
}
