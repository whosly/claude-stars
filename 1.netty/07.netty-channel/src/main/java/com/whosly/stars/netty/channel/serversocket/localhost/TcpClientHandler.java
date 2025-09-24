package com.whosly.stars.netty.channel.serversocket.localhost;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端消息处理器，处理服务器返回的消息
 *
 * @author fengyang
 * @date 2025-08-21 16:54:22
 * @description
 */
class TcpClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当收到服务器发送的消息时调用
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String response = (String) msg;
        System.out.println("客户端收到服务器响应: " + response);

        // 可以在这里添加后续业务逻辑，例如继续发送消息或关闭连接
        // ctx.writeAndFlush("Another message from client");
    }

    /**
     * 当通道激活（连接成功建立）时调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("客户端与服务器连接已建立");
    }

    /**
     * 发生异常时调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("客户端发生异常: " + cause.getMessage());
        cause.printStackTrace();
        // 发生异常时关闭通道
        ctx.close();
    }
}