package com.yueny.stars.netty.monitor.agent.core;

import com.yueny.stars.netty.monitor.agent.model.ChannelInfo;
import com.yueny.stars.netty.monitor.agent.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 监控Handler - 收集Channel信息并发送到监控服务器
 * 
 * @author fengyang
 */
public class MonitorHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = Logger.getLogger(MonitorHandler.class);
    private final MonitorAgent agent;
    private final java.util.List<Runnable> pendingEvents = new java.util.ArrayList<>();
    private ChannelHandlerContext ctx;
    
    public MonitorHandler(MonitorAgent agent) {
        this.agent = agent;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx; // 保存context引用
        
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            logger.info("✅ MonitorHandler: Channel active - %s", ctx.channel().id().asShortText());
            System.out.println("🔥 MonitorHandler: Channel active - " + ctx.channel().id().asShortText());
            
            // 如果监控代理已连接，立即发送；否则缓存事件
            if (agent != null && agent.isConnected()) {
                agent.sendChannelInfo(channelInfo, "CHANNEL_ACTIVE");
                logger.info("✅ MonitorHandler: Sent channel active immediately");
                System.out.println("📤 MonitorHandler: Sent channel active immediately for " + channelInfo.getChannelId());
            } else {
                // 缓存事件，等待连接建立后发送
                pendingEvents.add(() -> {
                    if (agent != null) {
                        agent.sendChannelInfo(channelInfo, "CHANNEL_ACTIVE");
                        logger.info("📤 MonitorHandler: Sent cached CHANNEL_ACTIVE for %s", channelInfo.getChannelId());
                    }
                });
                logger.info("📦 MonitorHandler: Cached CHANNEL_ACTIVE event (agent not connected yet)");
                
                // 启动一个任务定期检查连接状态并发送缓存的事件
                scheduleEventFlush();
            }
        } catch (Exception e) {
            logger.warn("Failed to handle channel active: %s", e.getMessage());
        }
        super.channelActive(ctx);
    }
    
    /**
     * 定期检查连接状态并发送缓存的事件
     */
    private void scheduleEventFlush() {
        if (pendingEvents.isEmpty()) return;
        
        // 使用EventLoop来调度任务
        ctx.channel().eventLoop().schedule(() -> {
            if (agent != null && agent.isConnected() && !pendingEvents.isEmpty()) {
                logger.info("🔄 MonitorHandler: Flushing %s cached events", pendingEvents.size());
                for (Runnable event : pendingEvents) {
                    try {
                        event.run();
                    } catch (Exception e) {
                        logger.warn("❌ Error flushing cached event:: %s", e.getMessage());
                    }
                }
                pendingEvents.clear();
            } else if (!pendingEvents.isEmpty()) {
                // 如果还没连接，继续等待
                scheduleEventFlush();
            }
        }, 2, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            ChannelInfo channelInfo = createChannelInfo(ctx);
            agent.sendChannelInfo(channelInfo, "CHANNEL_INACTIVE");
            logger.debug("Channel inactive: %s", ctx.channel().id().asShortText());
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

            // 收到的数据流量包大小
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
            
            // 添加详细的错误信息
            channelInfo.setErrorType(cause.getClass().getSimpleName());
            channelInfo.setStackTrace(getStackTrace(cause));
            
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
        
        // 尝试从channel属性中获取用户名
        try {
            Object usernameAttr = ctx.channel().attr(io.netty.util.AttributeKey.valueOf("username")).get();
            if (usernameAttr != null) {
                info.setUsername(usernameAttr.toString());
                logger.debug("Found username in channel attributes: %s", usernameAttr);
            }
        } catch (Exception e) {
            logger.debug("No username attribute found in channel: %s", e.getMessage());
        }
        
        // 根据地址信息判断channel角色
        // 如果remoteAddress包含服务器端口，说明这是客户端channel
        // 如果localAddress包含服务器端口，说明这是服务器端channel
        String role = determineChannelRole(info.getLocalAddress(), info.getRemoteAddress());
        info.setChannelRole(role);
        logger.debug("Channel role determined: %s for %s", role, info.getChannelId());
        
        return info;
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
        
        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = 0; i < Math.min(3, elements.length); i++) {
            sb.append("\tat ").append(elements[i].toString()).append("\n");
        }
        
        if (elements.length > 3) {
            sb.append("\t... ").append(elements.length - 3).append(" more\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 根据地址信息判断channel角色
     */
    private String determineChannelRole(String localAddress, String remoteAddress) {
        // 简单的启发式判断：
        // 1. 如果本地地址是服务器监听端口（如8080），则这是服务器端channel
        // 2. 如果远程地址是服务器端口，则这是客户端channel
        
        if (localAddress != null && localAddress.contains(":8080")) {
            return "SERVER";
        } else if (remoteAddress != null && remoteAddress.contains(":8080")) {
            return "CLIENT";
        }
        
        // 默认根据端口范围判断
        // 客户端通常使用高端口号连接
        if (localAddress != null) {
            try {
                String[] parts = localAddress.split(":");
                if (parts.length > 1) {
                    int port = Integer.parseInt(parts[parts.length - 1].replaceAll("[^0-9]", ""));
                    if (port >= 1024 && port < 8000) {
                        return "SERVER";
                    } else if (port >= 32768) {
                        return "CLIENT";
                    }
                }
            } catch (Exception e) {
                // 解析失败，使用默认逻辑
            }
        }
        
        return "UNKNOWN";
    }
}