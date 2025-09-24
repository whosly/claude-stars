package com.whosly.stars.netty.monitor.agent;

import com.whosly.stars.netty.monitor.agent.model.ChannelInfo;
import com.whosly.stars.netty.monitor.agent.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 本地监控Handler - 收集Channel信息并发送到监控服务器
 * 
 * @author fengyang
 */
public class LocalMonitorHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = Logger.getLogger(LocalMonitorHandler.class);
    
    private final LocalMonitorAgent agent;
    
    public LocalMonitorHandler(LocalMonitorAgent agent) {
        this.agent = agent;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            agent.sendChannelInfo(channelInfo, "CHANNEL_ACTIVE");
            logger.trace("Channel active: %s", ctx.channel().id().asShortText());
        } catch (Exception e) {
            logger.warn("Failed to send channel active info: %s", e.getMessage());
        }
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            agent.sendChannelInfo(channelInfo, "CHANNEL_INACTIVE");
            logger.trace("Channel inactive: %s", ctx.channel().id().asShortText());
        } catch (Exception e) {
            logger.warn("Failed to send channel inactive info: %s", e.getMessage());
        }
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            
            // 记录读取的数据大小
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                channelInfo.setBytesRead(channelInfo.getBytesRead() + buf.readableBytes());
            }
            
            agent.sendChannelInfo(channelInfo, "CHANNEL_READ");
            logger.trace("Channel read: %d bytes", channelInfo.getBytesRead());
        } catch (Exception e) {
            logger.warn("Failed to send channel read info: %s", e.getMessage());
        }
        super.channelRead(ctx, msg);
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            agent.sendChannelInfo(channelInfo, "CHANNEL_READ_COMPLETE");
        } catch (Exception e) {
            logger.warn("Failed to send channel read complete info: %s", e.getMessage());
        }
        super.channelReadComplete(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            channelInfo.setErrorMessage(cause.getMessage());
            agent.sendChannelInfo(channelInfo, "CHANNEL_EXCEPTION");
            logger.debug("Channel exception: %s", cause.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to send channel exception info: %s", e.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }
    
    /**
     * 创建Channel信息
     */
    private ChannelInfo createChannelInfo(ChannelHandlerContext ctx) {
        ChannelInfo info = new ChannelInfo();
        long currentTime = System.currentTimeMillis();
        
        info.setChannelId(ctx.channel().id().asShortText());
        info.setRemoteAddress(ctx.channel().remoteAddress() != null ? 
                ctx.channel().remoteAddress().toString() : "unknown");
        info.setLocalAddress(ctx.channel().localAddress() != null ? 
                ctx.channel().localAddress().toString() : "unknown");
        info.setActive(ctx.channel().isActive());
        info.setOpen(ctx.channel().isOpen());
        info.setWritable(ctx.channel().isWritable());
        
        // 设置状态
        if (!ctx.channel().isOpen()) {
            info.setState("CLOSED");
        } else if (!ctx.channel().isActive()) {
            info.setState("INACTIVE");
        } else {
            info.setState("ACTIVE");
        }
        
        // 设置时间戳
        info.setCreateTime(currentTime);
        info.setLastActiveTime(currentTime);
        info.setTimestamp(currentTime);
        
        // 设置EventLoop信息
        info.setEventLoopGroup(ctx.channel().eventLoop().getClass().getSimpleName());
        
        // 设置Pipeline信息
        StringBuilder pipelineInfo = new StringBuilder();
        ctx.pipeline().forEach(entry -> {
            if (pipelineInfo.length() > 0) {
                pipelineInfo.append(" -> ");
            }
            pipelineInfo.append(entry.getKey()).append("(")
                    .append(entry.getValue().getClass().getSimpleName()).append(")");
        });
        info.setPipeline(pipelineInfo.toString());
        
        return info;
    }
}