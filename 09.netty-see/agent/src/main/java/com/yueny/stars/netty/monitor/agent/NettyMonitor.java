package com.yueny.stars.netty.monitor.agent;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty监控工具类 - 支持本地Socket通信
 * 
 * @author fengyang
 */
@Slf4j
public class NettyMonitor {
    
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
            log.warn("NettyMonitor already initialized");
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
            
            log.info("NettyMonitor initialized for application: {} with socket/port: {}", applicationName, socketPathOrPort);
        } catch (Exception e) {
            log.warn("Failed to initialize NettyMonitor: {}", e.getMessage());
            initialized = false;
        }
    }
    
    /**
     * 获取监控Handler
     */
    public static ChannelHandler getMonitorHandler() {
        if (!initialized || agent == null) {
            log.debug("NettyMonitor not initialized, returning no-op handler");
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