package com.yueny.study.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 自定义 Alloc 行为 的 ByteBuf Wrapper
 *
 * @author fengyang
 * @date 2023/8/14 下午3:20
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
class WrappedAllocByteBuf
        extends WrappedByteBuf
{
    private ByteBufAllocator alloc;

    public WrappedAllocByteBuf(ByteBuf buf, ByteBufAllocator allocator)
    {
        super(buf);

        this.alloc = allocator;
    }

    @Override
    public ByteBufAllocator alloc() {
        throw new UnsupportedOperationException("Calling the alloc method is not allowed in a custom(StoneData) WrappedAllocByteBuf");
    }

}
