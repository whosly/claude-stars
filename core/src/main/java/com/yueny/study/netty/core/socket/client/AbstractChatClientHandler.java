package com.yueny.study.netty.core.socket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengyang
 * @date 2025-08-18 14:32:56
 * @description
 */
public class AbstractChatClientHandler extends SimpleChannelInboundHandler<String> {
    /**
     * Print chat message received from server.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Message: " + msg);
    }

    /**
     * 连接建立时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("已连接到聊天服务器！");
        super.channelActive(ctx);
    }

    /**
     * 连接断开时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与服务器的连接已断开");
        super.channelInactive(ctx);
    }

    /**
     * 发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("客户端发生异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

}
