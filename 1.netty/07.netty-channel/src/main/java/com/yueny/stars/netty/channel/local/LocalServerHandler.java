package com.yueny.stars.netty.channel.local;

/**
 * 本地服务器处理器
 *
 * @author fengyang
 * @date 2025-08-21 16:32:34
 * @description
 */
class LocalServerHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
        String message = (String) msg;
        System.out.println("本地服务器收到消息：" + message);
        // 回复客户端
        ctx.writeAndFlush("本地服务器已收到：" + message);
    }
}
