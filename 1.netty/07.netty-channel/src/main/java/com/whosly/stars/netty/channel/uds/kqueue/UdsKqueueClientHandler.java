package com.whosly.stars.netty.channel.uds.kqueue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengyang
 * @date 2025-08-21 11:17:58
 * @description
 */
public class UdsKqueueClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("[UDS Client] 收到: " + msg);
        ctx.close();
    }
}
