package com.whosly.stars.netty.channel.socket;

/**
 * 服务器心跳处理器，用于处理空闲状态事件
 *
 * @author fengyang
 * @date 2025-08-21 17:07:42
 * @description
 */
class ServerHeartbeatHandler extends io.netty.channel.ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(io.netty.channel.ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
            io.netty.handler.timeout.IdleStateEvent event = (io.netty.handler.timeout.IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    System.out.println("读空闲，客户端可能已断开: " + ctx.channel().remoteAddress());
                    break;
                case WRITER_IDLE:
                    System.out.println("写空闲，发送心跳检测: " + ctx.channel().remoteAddress());
                    ctx.writeAndFlush("SERVER_HEARTBEAT");
                    break;
                case ALL_IDLE:
                    System.out.println("读写空闲，关闭连接: " + ctx.channel().remoteAddress());
                    ctx.close();
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
