package com.whosly.stars.netty.msgpack.client;

import com.whosly.stars.netty.msgpack.domain.HeartbeatData;
import com.whosly.stars.netty.msgpack.domain.TypeData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author fengyang
 * @date 2025-08-28 14:12:34
 * @description
 */
class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {

        // client 发送的数据对象
        for (int i = 1; i <= 10; i++) {
            try {
                HeartbeatData msg = HeartbeatData.builder()
                        .type(TypeData.PING)
                        .seatId(TypeData.PING_SEAT)
                        .memo("jack a dull boy" + i)
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
}
