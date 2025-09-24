package com.whosly.stars.netty.msgpack.server;

import com.whosly.stars.netty.msgpack.domain.HeartbeatData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengyang
 * @date 2025-08-28 14:09:26
 * @description
 */
class ServerHandler extends SimpleChannelInboundHandler<HeartbeatData> {
    private int counter=0;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HeartbeatData msg) {
        System.out.println("channelRead - HeartbeatData：" + msg + "， counter:" + (++counter));
    }
}