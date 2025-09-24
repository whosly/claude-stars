package com.whosly.stars.netty.channel.serversocket.loopback;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class TcpLoopbackClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("[Client] 收到: " + msg.trim());
        ctx.close();
    }
}
