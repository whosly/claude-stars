package com.whosly.stars.netty.msgpack.server;

import com.whosly.stars.netty.msgpack.domain.HeartbeatDataV6;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengyang
 * @date 2025-08-28 10:54:26
 * @description
 */
class ServerHandler extends SimpleChannelInboundHandler<HeartbeatDataV6> {
    private int counter=0;

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("Client " + ctx.channel().remoteAddress() + " connected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HeartbeatDataV6 msg) {
        System.out.println("channelRead - HeartbeatDataV6：" + msg + "， counter:" + (++counter));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        ctx.close();
    }
}