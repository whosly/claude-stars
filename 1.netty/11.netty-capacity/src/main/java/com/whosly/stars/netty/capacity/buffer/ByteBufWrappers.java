package com.whosly.stars.netty.capacity.buffer;

import com.whosly.stars.netty.capacity.allocator.Allocators;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * ByteBuf wrapper
 *
 * @author fengyang
 * @date 2023/8/15 下午2:12
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public final class ByteBufWrappers
{
    /**
     *
     */
    public static final WrappedAutoFlushByteBuf wrapper(ByteBuf buf, ByteBufAllocator allocator)
    {
        return new WrappedAutoFlushByteBuf(4 * Allocators._1MB, buf, allocator);
    }

    public static final WrappedAutoFlushByteBuf wrapper(long autoFlushSize, ByteBuf buf, ByteBufAllocator allocator)
    {
        return new WrappedAutoFlushByteBuf(autoFlushSize, buf, allocator);
    }
}