package com.whosly.stars.netty.msgpack.heartbeat.client;

import com.whosly.stars.netty.msgpack.domain.HeartbeatData;
import com.whosly.stars.netty.msgpack.heartbeat.handler.AbstractChannelInboundHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author fengyang
 * @date 2025-08-28 17:33:01
 * @description
 */
class ClientHandler extends AbstractChannelInboundHandler {

    public ClientHandler() {
        super("client");
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, Object msg) {
        HeartbeatData deviceValue = (HeartbeatData) msg;
        System.out.println("接收数据:" + deviceValue);

//        HeartbeatData heartbeatData = new HeartbeatData();
//        heartbeatData.setSeatId(TypeData.SERVER_RESPONSE);
//        channelHandlerContext.writeAndFlush(heartbeatData);
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);

        sendPingMsg(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(name + " exception"+cause.toString());
    }
}
