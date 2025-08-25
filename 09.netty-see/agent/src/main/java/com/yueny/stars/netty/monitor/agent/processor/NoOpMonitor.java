package com.yueny.stars.netty.monitor.agent.processor;

import com.yueny.stars.netty.monitor.agent.util.Logger;
import io.netty.channel.ChannelHandlerContext;

/**
 * 空操作监控器
 * 用于初始化失败时的降级处理，确保系统正常运行
 * 
 * @author fengyang
 */
public class NoOpMonitor {
    
    private static final Logger logger = Logger.getLogger(NoOpMonitor.class);
    
    private final String context;
    private final String reason;
    
    public NoOpMonitor(String context, String reason) {
        this.context = context;
        this.reason = reason;
        logger.warn("创建NoOp监控器 [%s]: %s", context, reason);
    }
    
    /**
     * 空操作的连接监控
     */
    public void onConnect(ChannelHandlerContext ctx) {
        // 什么都不做，但记录调试日志
        if (logger.isDebugEnabled()) {
            logger.debug("NoOp监控器 [%s] - 忽略连接事件: %s", context, ctx.channel());
        }
    }
    
    /**
     * 空操作的断开监控
     */
    public void onDisconnect(ChannelHandlerContext ctx) {
        // 什么都不做，但记录调试日志
        if (logger.isDebugEnabled()) {
            logger.debug("NoOp监控器 [%s] - 忽略断开事件: %s", context, ctx.channel());
        }
    }
    
    /**
     * 空操作的消息监控
     */
    public void onMessage(ChannelHandlerContext ctx, Object msg) {
        // 什么都不做，但记录调试日志
        if (logger.isDebugEnabled()) {
            logger.debug("NoOp监控器 [%s] - 忽略消息事件: %s", context, msg.getClass().getSimpleName());
        }
    }
    
    /**
     * 空操作的异常监控
     */
    public void onException(ChannelHandlerContext ctx, Throwable cause) {
        // 什么都不做，但记录调试日志
        if (logger.isDebugEnabled()) {
            logger.debug("NoOp监控器 [%s] - 忽略异常事件: %s", context, cause.getMessage());
        }
    }
    
    /**
     * 获取监控器状态信息
     */
    public String getStatus() {
        return String.format("NoOp监控器 [%s] - 原因: %s", context, reason);
    }
    
    /**
     * 检查是否可以尝试恢复
     */
    public boolean canRecover() {
        // NoOp监控器通常不能自动恢复，需要外部重新初始化
        return false;
    }
    
    /**
     * 获取降级原因
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * 获取上下文信息
     */
    public String getContext() {
        return context;
    }
    
    @Override
    public String toString() {
        return String.format("NoOpMonitor{context='%s', reason='%s'}", context, reason);
    }
}