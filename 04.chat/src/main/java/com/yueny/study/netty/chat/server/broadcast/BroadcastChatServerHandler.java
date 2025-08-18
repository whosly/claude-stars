package com.yueny.study.netty.chat.server.broadcast;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 广播聊天
 *
 * @author fengyang
 * @date 2025-08-18 14:28:40
 * @description
 */
public class BroadcastChatServerHandler extends SimpleChannelInboundHandler<String> {
    // List of connected client channels.
    static final List<Channel> channels = new ArrayList<Channel>();

    /*
     * Whenever client connects to server through channel, add his channel to the
     * list of channels.
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        System.out.println("Client " + ctx.channel().remoteAddress() + " connected");

        channels.add(ctx.channel());
    }

    /*
     * When a message is received from client, send that message to all channels.
     * FOr the sake of simplicity, currently we will send received chat message to
     * all clients instead of one specific client. This code has scope to improve to
     * send message to specific client as per senders choice.
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Server received - " + msg);

        for (Channel c : channels) {
            c.writeAndFlush("-> " + msg + '\n');
        }
    }

    /*
     * In case of exception, close channel. One may chose to custom handle exception
     * & have alternative logical flows.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Closing connection for client - " + ctx);
        ctx.close();
    }
}
