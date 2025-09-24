package com.whosly.stars.netty.capacity.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

/**
 * 达到指定大小后自动 flush 的 ByteBuf.
 *
 * 默认 4MB  flush
 *
 * @author fengyang
 * @date 2023/8/14 下午4:53
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class WrappedAutoFlushByteBuf
        extends WrappedAllocReleaseByteBuf
{
    /**
     * 自动 flush 的 size
     */
    private long autoFlushSize;

    protected WrappedAutoFlushByteBuf(long autoFlushSize, ByteBuf buf, ByteBufAllocator allocator)
    {
        super(buf, allocator);

        this.autoFlushSize = autoFlushSize;
    }

    @Override
    public ByteBuf writeBoolean(boolean value)
    {
        return super.writeBoolean(value);
    }

    @Override
    public ByteBuf writeByte(int value)
    {
        return super.writeByte(value);
    }

    @Override
    public ByteBuf writeShort(int value)
    {
        return super.writeShort(value);
    }

    @Override
    public ByteBuf writeShortLE(int value)
    {
        return super.writeShortLE(value);
    }

    @Override
    public ByteBuf writeMedium(int value)
    {
        return super.writeMedium(value);
    }

    @Override
    public ByteBuf writeMediumLE(int value)
    {
        return super.writeMediumLE(value);
    }

    @Override
    public ByteBuf writeInt(int value)
    {
        return super.writeInt(value);
    }

    @Override
    public ByteBuf writeIntLE(int value)
    {
        return super.writeIntLE(value);
    }

    @Override
    public ByteBuf writeLong(long value)
    {
        return super.writeLong(value);
    }

    @Override
    public ByteBuf writeLongLE(long value)
    {
        return super.writeLongLE(value);
    }

    @Override
    public ByteBuf writeChar(int value)
    {
        return super.writeChar(value);
    }

    @Override
    public ByteBuf writeFloat(float value)
    {
        return super.writeFloat(value);
    }

    @Override
    public ByteBuf writeDouble(double value)
    {
        return super.writeDouble(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src)
    {
        return super.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length)
    {
        return super.writeBytes(src, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
    {
        return super.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] src)
    {
        return super.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
    {
        return super.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer src)
    {
        return super.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length)
            throws IOException
    {
        return super.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length)
            throws IOException
    {
        return super.writeBytes(in, length);
    }

    @Override
    public int writeBytes(FileChannel in, long position, int length)
            throws IOException
    {
        return super.writeBytes(in, position, length);
    }

    @Override
    public ByteBuf writeZero(int length)
    {
        return super.writeZero(length);
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset)
    {
        return super.writeCharSequence(sequence, charset);
    }
}
