package com.yueny.stars.netty.msgpack.server;

import com.yueny.stars.netty.msgpack.domain.Student6Info;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengyang
 * @date 2025-08-28 10:54:26
 * @description
 */
public class ServerHandler extends SimpleChannelInboundHandler<Student6Info> {
    private int counter=0;

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("Client " + ctx.channel().remoteAddress() + " connected");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Student6Info msg) {
        System.out.println("channelRead - StudentInfo：" + msg + "， counter:"+ ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        ctx.close();
    }
}