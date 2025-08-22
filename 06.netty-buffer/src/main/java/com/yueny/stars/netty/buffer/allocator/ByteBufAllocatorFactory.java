package com.yueny.stars.netty.buffer.allocator;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * ByteBufAllocator 实例化工厂
 *
 * @author fengyang
 * @date 2025-08-20 14:05:02
 * @description
 */
public class ByteBufAllocatorFactory {
    private static final Channel CHANNEL_FROM_NIOSOCKET = new NioSocketChannel();

    /**
     * 通过 ChannelHandlerContext 获得 ByteBufAllocator 的方式【基于堆内存存储】
     *
     * 在 Netty 的 ChannelHandler 或 ChannelInboundHandlerAdapter 回调中，优先用 ctx.alloc() 或 channel.alloc()。
     */
    public static ByteBufAllocator allocatorByChannel(ChannelHandlerContext ctx) {
        // 安全降级处理：当 ctx 为 null 时返回 ByteBufAllocator.DEFAULT，避免 NPE
        if (ctx == null) {
            return ByteBufAllocator.DEFAULT;
        }
        return ctx.alloc();
    }

    /**
     * 通过 Channel 获得 ByteBufAllocator 的方式【基于堆内存存储】
     *
     * 在 Netty 的 ChannelHandler 或 ChannelInboundHandlerAdapter 回调中，优先用 ctx.alloc() 或 channel.alloc()。
     */
    public static ByteBufAllocator allocatorByChannel(Channel channel) {
        if (channel == null) {
            return ByteBufAllocator.DEFAULT;
        }
        return channel.alloc();
    }

    /**
     * 返回默认分配器【基于堆外内存存储的】（可能是池化实现，取决于平台配置）
     * <p>
     *
     * 不建议使用。 使用场景在非回调环境（如 main 方法或测试）中：
     * @see ByteBufAllocatorFactory#defaultAllocator()
     * @see ByteBufAllocatorFactory#pooledAllocator()
     * @see ByteBufAllocatorFactory#unpooledAllocator()
     * @see ByteBufAllocatorFactory#allocatorByNioSocket()
     */
    public static ByteBufAllocator defaultAllocator() {
        return ByteBufAllocator.DEFAULT;
    }

    /**
     * 返回池化分配器【基于堆外内存存储的】。 默认 PlatformDependent.directBufferPreferred()
     * <p>
     *
     * 不建议使用。 使用场景在非回调环境（如 main 方法或测试）中：
     * @see ByteBufAllocatorFactory#defaultAllocator()
     * @see ByteBufAllocatorFactory#pooledAllocator()
     * @see ByteBufAllocatorFactory#unpooledAllocator()
     * @see ByteBufAllocatorFactory#allocatorByNioSocket()
     */
    public static ByteBufAllocator pooledAllocator() {
        return PooledByteBufAllocator.DEFAULT;
    }

    /**
     * 返回未池化分配器【基于堆外内存存储的】
     * <p>
     *
     * 不建议使用。 使用场景在非回调环境（如 main 方法或测试）中：
     * @see ByteBufAllocatorFactory#defaultAllocator()
     * @see ByteBufAllocatorFactory#pooledAllocator()
     * @see ByteBufAllocatorFactory#unpooledAllocator()
     * @see ByteBufAllocatorFactory#allocatorByNioSocket()
     */
    public static ByteBufAllocator unpooledAllocator() {
        return UnpooledByteBufAllocator.DEFAULT;
    }

    /**
     * 通过手动实例化 NioSocketChannel 的方式获得 ByteBufAllocator【基于堆外内存存储的】
     * <p>
     *
     * 不建议使用。 使用场景在非回调环境（如 main 方法或测试）中：
     * @see ByteBufAllocatorFactory#defaultAllocator()
     * @see ByteBufAllocatorFactory#pooledAllocator()
     * @see ByteBufAllocatorFactory#unpooledAllocator()
     * @see ByteBufAllocatorFactory#allocatorByNioSocket()
     */
    public static ByteBufAllocator allocatorByNioSocket() {

        return CHANNEL_FROM_NIOSOCKET.alloc();
    }

}
