package com.whosly.stars.netty.monitor.agent.processor;

import com.whosly.stars.netty.monitor.agent.annotation.NettyMonitor;
import com.whosly.stars.netty.monitor.agent.core.MonitorAgent;
import com.whosly.stars.netty.monitor.agent.util.Logger;

import java.lang.reflect.Method;

/**
 * 监控注解处理器
 * 
 * @author fengyang
 */
public class MonitorAnnotationProcessor {
    
    private static final Logger logger = Logger.getLogger(MonitorAnnotationProcessor.class);
    
    /**
     * 处理类级别的注解
     */
    public static void processClass(Class<?> clazz) {
        NettyMonitor annotation = clazz.getAnnotation(NettyMonitor.class);
        if (annotation != null && annotation.enabled()) {
            String applicationName = getApplicationName(annotation, clazz.getSimpleName());
            initializeMonitor(applicationName, annotation);
        }
    }
    
    /**
     * 处理方法级别的注解
     */
    public static void processMethod(Method method, Object instance) {
        NettyMonitor annotation = method.getAnnotation(NettyMonitor.class);
        if (annotation != null && annotation.enabled()) {
            String applicationName = getApplicationName(annotation, 
                    instance.getClass().getSimpleName() + "." + method.getName());
            initializeMonitor(applicationName, annotation);
        }
    }
    
    /**
     * 自动扫描并处理注解
     */
    public static void scanAndProcess(String packageName) {
        try {
            // 简单的包扫描实现
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            
            // 这里可以实现更复杂的包扫描逻辑
            logger.info("Scanning package: %s for @NettyMonitor annotations", packageName);
            
        } catch (Exception e) {
            logger.error("Failed to scan package: %s", e, packageName);
        }
    }
    
    /**
     * 获取应用名称
     */
    private static String getApplicationName(NettyMonitor annotation, String defaultName) {
        if (!annotation.value().isEmpty()) {
            return annotation.value();
        }
        if (!annotation.applicationName().isEmpty()) {
            return annotation.applicationName();
        }
        return defaultName;
    }
    
    /**
     * 初始化监控代理
     */
    private static void initializeMonitor(String applicationName, NettyMonitor annotation) {
        try {
            MonitorAgent.initialize(applicationName, annotation.host(), annotation.port());
            logger.info("Monitor initialized for application: %s", applicationName);
        } catch (Exception e) {
            logger.error("Failed to initialize monitor for application: %s", e, applicationName);
        }
    }
}