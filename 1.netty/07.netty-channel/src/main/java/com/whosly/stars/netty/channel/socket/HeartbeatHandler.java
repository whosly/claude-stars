package com.whosly.stars.netty.channel.socket;

/**
 * 心跳处理器，用于处理空闲状态事件
 *
 * @author fengyang
 * @date 2025-08-21 17:10:38
 * @description
 */
class HeartbeatHandler extends io.netty.channel.ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(io.netty.channel.ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
            io.netty.handler.timeout.IdleStateEvent event = (io.netty.handler.timeout.IdleStateEvent) evt;
            if (event.state() == io.netty.handler.timeout.IdleState.WRITER_IDLE) {
                // 发送心跳消息
                ctx.writeAndFlush("HEARTBEAT");
                System.out.println("发送心跳包");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
