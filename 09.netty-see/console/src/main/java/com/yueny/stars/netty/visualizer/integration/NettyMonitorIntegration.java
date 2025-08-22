package com.yueny.stars.netty.visualizer.integration;

import com.yueny.stars.netty.visualizer.service.NettyMonitorService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty监控集成工具
 * 
 * @author fengyang
 */
@Slf4j
@Component
public class NettyMonitorIntegration {
    
    private static NettyMonitorService monitorService;
    
    @Autowired
    public void setMonitorService(NettyMonitorService monitorService) {
        NettyMonitorIntegration.monitorService = monitorService;
    }
    
    /**
     * 获取监控Handler，用于自动注册Channel
     */
    public static MonitorHandler getMonitorHandler() {
        return new MonitorHandler();
    }
    
    /**
     * 手动注册Channel
     */
    public static void registerChannel(Channel channel) {
        if (monitorService != null) {
            monitorService.registerChannel(channel);
        }
    }
    
    /**
     * 监控Handler，自动注册和注销Channel
     */
    public static class MonitorHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            if (monitorService != null) {
                monitorService.registerChannel(ctx.channel());
                log.info("Channel registered for monitoring: {}", ctx.channel().id().asShortText());
            }
            super.channelActive(ctx);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (monitorService != null) {
                monitorService.unregisterChannel(ctx.channel().id().asShortText());
                log.info("Channel unregistered from monitoring: {}", ctx.channel().id().asShortText());
            }
            super.channelInactive(ctx);
        }
    }
}