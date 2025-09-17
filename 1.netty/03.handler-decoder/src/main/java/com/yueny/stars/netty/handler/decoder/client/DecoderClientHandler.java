package com.yueny.stars.netty.handler.decoder.client;

import com.yueny.stars.netty.handler.decoder.domain.RequestData;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author fengyang
 * @date 2025-08-18 10:09:10
 * @description
 */
public class DecoderClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {

        // client 发送的数据对象
        RequestData msg = RequestData.builder()
                .intValue(123)
                .stringValue("all work and no play makes jack a dull boy (client)")
                .build();

        ChannelFuture future = ctx.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("DecoderClient received: " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}