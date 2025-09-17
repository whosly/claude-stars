package com.yueny.stars.netty.monitor.agent;

import com.yueny.stars.netty.monitor.agent.annotation.NettyMonitor;
import com.yueny.stars.netty.monitor.agent.core.MonitorAgent;
import com.yueny.stars.netty.monitor.agent.processor.MonitorAnnotationProcessor;
import com.yueny.stars.netty.monitor.agent.util.Logger;
import io.netty.channel.ChannelHandler;

/**
 * Netty监控代理主入口类
 * 
 * @author fengyang
 */
public class NettyMonitorAgent {
    
    private static final Logger logger = Logger.getLogger(NettyMonitorAgent.class);
    
    /**
     * 通过注解启动监控
     */
    public static void enableMonitoring(Class<?> clazz) {
        MonitorAnnotationProcessor.processClass(clazz);
    }
    
    /**
     * 手动启动监控
     */
    public static void initialize(String applicationName) {
        initialize(applicationName, "localhost", 19999);
    }
    
    /**
     * 手动启动监控（指定服务器地址）
     */
    public static void initialize(String applicationName, String host, int port) {
        MonitorAgent.initialize(applicationName, host, port);
    }
    
    /**
     * 获取监控Handler
     */
    public static ChannelHandler getMonitorHandler() {
        return MonitorAgent.getMonitorHandler();
    }
    
    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return MonitorAgent.getInstance() != null;
    }
    
    /**
     * 自动扫描并启用监控
     */
    public static void scanAndEnable(String packageName) {
        MonitorAnnotationProcessor.scanAndProcess(packageName);
    }
    
    /**
     * 关闭监控
     */
    public static void shutdown() {
        MonitorAgent instance = MonitorAgent.getInstance();
        if (instance != null) {
            instance.shutdown();
        }
    }
}