package com.whosly.stars.netty.buffer.buf;

import com.whosly.stars.netty.buffer.allocator.ByteBufAllocatorFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

/**
 * @author fengyang
 * @date 2025-08-20 15:43:00
 * @description
 */
public class ByteBufExample {
    /**
     * 通过 ByteBufAllocatorFactory(也就是ByteBufAllocator) 的方式创建一个【池化的基于堆外内存存储的】ByteBuf 实例
     */
    public static ByteBuf createdByPooledAllocator() {
        ByteBufAllocator allocator = ByteBufAllocatorFactory.pooledAllocator();

        return allocator.buffer();
    }

    /**
     * 通过 Unpooled 的方式创建一个【未池化的基于堆内存存储的】 ByteBuf 实例
     */
    public static ByteBuf createdByUnpooled() {
        return Unpooled.buffer();
    }

}
