package com.whosly.stars.netty.msgpack.heartbeat.server;

import com.whosly.stars.netty.msgpack.domain.HeartbeatData;
import com.whosly.stars.netty.msgpack.domain.TypeData;
import com.whosly.stars.netty.msgpack.heartbeat.handler.AbstractChannelInboundHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author fengyang
 * @date 2025-08-28 17:35:31
 * @description
 */
class ServerHandler extends AbstractChannelInboundHandler {
    public ServerHandler() {
        super("server");
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, Object msg) {
        HeartbeatData deviceValue = (HeartbeatData) msg;
        System.out.println("server 接收数据:" + deviceValue);

        HeartbeatData s = new HeartbeatData();
        s.setType(TypeData.CUSTOME);
        s.setSpeed(0);
        s.setSeatId(TypeData.SERVER_RESPONSE);
        s.setMemo("server 发送数据");

        System.out.println("server 发送数据:" + s);
        channelHandlerContext.writeAndFlush(s);
    }

    @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);

        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }

    @Override
    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        super.handleWriterIdle(ctx);

        ctx.writeAndFlush("心跳检测\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(name+" exception"+cause.toString());
    }
}
