package com.whosly.stars.netty.channel.socket;

/**
 * 客户端处理器
 *
 * @author fengyang
 * @date 2025-08-21 17:00:00
 * @description
 */
class TcpClientHandler extends io.netty.channel.ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接已建立，可以开始通信");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
        String message = (String) msg;
        System.out.println("收到服务器消息: " + message);
    }

    @Override
    public void channelInactive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接已断开");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("发生异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
