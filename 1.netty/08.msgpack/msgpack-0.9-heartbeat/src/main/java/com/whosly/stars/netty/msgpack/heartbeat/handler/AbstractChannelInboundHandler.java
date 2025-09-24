package com.whosly.stars.netty.msgpack.heartbeat.handler;

import com.whosly.stars.netty.msgpack.domain.HeartbeatData;
import com.whosly.stars.netty.msgpack.domain.TypeData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleState;

/**
 * @author fengyang
 * @date 2025-08-28 17:14:24
 * @description
 */
public abstract class AbstractChannelInboundHandler extends ChannelInboundHandlerAdapter { // SimpleChannelInboundHandler<HeartbeatData>
    protected String name;
    private int heartbeatCount = 0;
    private int readCount = 0;

    public AbstractChannelInboundHandler(String name) {
        this.name = name;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ++readCount;

        HeartbeatData deviceValue = (HeartbeatData) msg;
        int type = deviceValue.getType();
        System.out.println("received HeartbeatData type="+type);

        switch (type){
            case 1:
                sendPongMsg(ctx);
                break;

            case 2:
                System.out.println(name + " get pong msg from " + ctx.channel().remoteAddress());

                break;

            case 3:
                handleData(ctx, msg);

                break;
        }

    }

    protected void sendPingMsg(ChannelHandlerContext context) {
        HeartbeatData deviceValue = new HeartbeatData();
        deviceValue.setType(TypeData.PING);
        deviceValue.setSpeed(0);
        deviceValue.setSeatId(TypeData.PING_SEAT);
        deviceValue.setMemo("sendPingMsg");

        context.channel().writeAndFlush(deviceValue);
        heartbeatCount++;
        System.out.println(name + " sent ping msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
    }

    private void sendPongMsg(ChannelHandlerContext context) {
        HeartbeatData deviceValue = new HeartbeatData();
        deviceValue.setType(TypeData.PONG);
        deviceValue.setSpeed(0);
        deviceValue.setSeatId(TypeData.PONG_SEAT);
        deviceValue.setMemo("sendPongMsg");

        context.channel().writeAndFlush(deviceValue);
        heartbeatCount++;
        System.out.println(name + " sent pong msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
    }

    protected abstract void handleData(ChannelHandlerContext channelHandlerContext, Object msg);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                System.out.println("读空闲，关闭连接: " + ctx.channel());
                handleReaderIdle(ctx);
            } else if (e.state() == IdleState.WRITER_IDLE) {
                System.out.println("写空闲，发送心跳: " + ctx.channel());

                handleWriterIdle(ctx);
            } else if (e.state() == IdleState.ALL_IDLE) {
                System.out.println("读写都空闲: " + ctx.channel());

                handleAllIdle(ctx);
            }
        }

        // 传递事件给下一个处理器
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("---" + ctx.channel().remoteAddress() + " is active---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("---" + ctx.channel().remoteAddress() + " is inactive---");
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        System.err.println("---READER_IDLE---");
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
    }
}
