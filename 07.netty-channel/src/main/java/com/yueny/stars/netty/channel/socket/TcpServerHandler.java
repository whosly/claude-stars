package com.yueny.stars.netty.channel.socket;

/**
 * 服务器业务处理器
 *
 * @author fengyang
 * @date 2025-08-21 17:07:21
 * @description
 */
class TcpServerHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
    private int connectionCount = 0;

    @Override
    public void channelActive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        connectionCount++;
        System.out.println("客户端连接成功: " + ctx.channel().remoteAddress());
        System.out.println("当前连接数: " + connectionCount);
        // 发送欢迎消息
        ctx.writeAndFlush("欢迎连接到服务器! 你是第" + connectionCount + "个连接");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
        String message = (String) msg;
        System.out.println("收到客户端消息: " + message + " from " + ctx.channel().remoteAddress());

        // 如果是心跳包，直接回复
        if ("HEARTBEAT".equals(message)) {
            ctx.writeAndFlush("HEARTBEAT_RESPONSE");
            return;
        }

        // 回声处理：将收到的消息发回给客户端
        String response = "服务器回复: " + message;
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelInactive(io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        connectionCount--;
        System.out.println("客户端断开连接: " + ctx.channel().remoteAddress());
        System.out.println("当前连接数: " + connectionCount);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(io.netty.channel.ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("服务器处理异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
