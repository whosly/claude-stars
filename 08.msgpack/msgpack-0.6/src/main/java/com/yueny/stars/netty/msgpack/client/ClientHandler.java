package com.yueny.stars.netty.msgpack.client;

import com.yueny.stars.netty.msgpack.domain.Student6Info;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author fengyang
 * @date 2025-08-28 10:57:02
 * @description
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {

        // client 发送的数据对象
        for (int i = 1; i <= 10; i++) {
            try {
                Student6Info msg = Student6Info.builder()
                        .age(i)
                        .name("jack a dull boy"+i)
                        .build();

                ctx.writeAndFlush(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("Client received: " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
