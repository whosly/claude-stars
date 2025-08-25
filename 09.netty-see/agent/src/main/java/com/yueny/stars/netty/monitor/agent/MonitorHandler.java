package com.yueny.stars.netty.monitor.agent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 轻量级监控Handler
 * 
 * @author fengyang
 */
public class MonitorHandler extends ChannelInboundHandlerAdapter {
    
    private final MonitorAgent agent;
    
    public MonitorHandler() {
        this.agent = MonitorAgent.getInstance();
    }
    
    public MonitorHandler(String monitorUrl) {
        this.agent = MonitorAgent.getInstance(monitorUrl);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        agent.registerChannel(ctx.channel());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        agent.unregisterChannel(ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        agent.updateChannel(ctx.channel());
        super.channelRead(ctx, msg);
    }
}