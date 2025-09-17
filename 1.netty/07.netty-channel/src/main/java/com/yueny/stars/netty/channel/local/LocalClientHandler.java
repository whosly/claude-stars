package com.yueny.stars.netty.channel.local;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengyang
 * @date 2025-08-21 16:33:24
 * @description
 */
class LocalClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("客户端收到回复：" + msg);
        // 收到回复后关闭
        ctx.close();
    }
}
