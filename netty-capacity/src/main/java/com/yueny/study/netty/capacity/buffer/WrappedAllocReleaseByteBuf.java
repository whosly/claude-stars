package com.yueny.study.netty.capacity.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 扩展增强 Release 的 ByteBuf Wrapper
 *
 * @author fengyang
 * @date 2023/8/15 下午12:39
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
class WrappedAllocReleaseByteBuf
        extends WrappedAllocByteBuf
{
    protected WrappedAllocReleaseByteBuf(ByteBuf buf, ByteBufAllocator allocator)
    {
        super(buf, allocator);
    }

    @Override
    public boolean release()
    {
        // donothing
        // ByteBufPoolManager.getInstance().release

        return super.release();
    }

    @Override
    public boolean release(int decrement)
    {
        // donothing
        // ByteBufPoolManager.getInstance().release

        return super.release(decrement);
    }
}
