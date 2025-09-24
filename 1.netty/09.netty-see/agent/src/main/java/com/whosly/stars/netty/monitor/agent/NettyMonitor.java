package com.whosly.stars.netty.monitor.agent;

import com.whosly.stars.netty.monitor.agent.util.Logger;
import io.netty.channel.ChannelHandler;

/**
 * Netty监控工具类 - 支持本地Socket通信
 * 
 * @author fengyang
 */
public class NettyMonitor {
    
    private static final Logger logger = Logger.getLogger(NettyMonitor.class);
    
    private static LocalMonitorAgent agent;
    private static boolean initialized = false;
    
    /**
     * 初始化监控（使用默认本地Socket路径）
     */
    public static void initialize(String applicationName) {
        initialize(applicationName, "/tmp/netty-monitor.sock");
    }
    
    /**
     * 初始化监控（指定本地Socket路径或TCP端口）
     */
    public static void initialize(String applicationName, String socketPathOrPort) {
        if (initialized) {
            logger.warn("NettyMonitor already initialized");
            return;
        }
        
        try {
            agent = new LocalMonitorAgent(applicationName, socketPathOrPort);
            agent.start();
            initialized = true;
            
            // 添加JVM关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (agent != null) {
                    agent.shutdown();
                }
            }));
            
            logger.info("NettyMonitor initialized for application: %s with socket/port: %s", applicationName, socketPathOrPort);
        } catch (Exception e) {
            logger.warn("Failed to initialize NettyMonitor: %s", e.getMessage());
            initialized = false;
        }
    }
    
    /**
     * 获取监控Handler
     */
    public static ChannelHandler getMonitorHandler() {
        if (!initialized || agent == null) {
            logger.debug("NettyMonitor not initialized, returning no-op handler");
            return new NoOpHandler();
        }
        return new LocalMonitorHandler(agent);
    }
    
    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return initialized && agent != null;
    }
    
    /**
     * 关闭监控
     */
    public static void shutdown() {
        if (agent != null) {
            agent.shutdown();
            agent = null;
        }
        initialized = false;
    }
    
    /**
     * 空操作Handler，用于未初始化时
     */
    private static class NoOpHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
        // 什么都不做
    }
}