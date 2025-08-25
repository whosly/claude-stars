package com.yueny.stars.netty.monitor.agent.context;

import com.yueny.stars.netty.monitor.agent.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控上下文管理器
 * 管理全局和线程本地的上下文变量，支持动态变量注入
 * 
 * @author fengyang
 */
public class MonitorContextManager {
    
    private static final Logger logger = Logger.getLogger(MonitorContextManager.class);
    
    // 线程本地上下文
    private static final ThreadLocal<Map<String, Object>> THREAD_CONTEXT = 
            ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    // 全局上下文
    private static final Map<String, Object> GLOBAL_CONTEXT = new ConcurrentHashMap<>();
    
    // 调试模式标志
    private static volatile boolean debugMode = false;
    
    /**
     * 设置全局上下文变量
     * 
     * @param key 变量名
     * @param value 变量值
     */
    public static void setGlobalContext(String key, Object value) {
        if (key == null) {
            logger.warn("尝试设置null键的全局上下文变量");
            return;
        }
        
        Object oldValue = GLOBAL_CONTEXT.put(key, value);
        
        if (debugMode) {
            logger.debug("设置全局上下文: %s = %s (旧值: %s)", key, value, oldValue);
        }
    }
    
    /**
     * 设置线程本地上下文变量
     * 
     * @param key 变量名
     * @param value 变量值
     */
    public static void setThreadContext(String key, Object value) {
        if (key == null) {
            logger.warn("尝试设置null键的线程上下文变量");
            return;
        }
        
        Map<String, Object> threadContext = THREAD_CONTEXT.get();
        Object oldValue = threadContext.put(key, value);
        
        if (debugMode) {
            logger.debug("设置线程上下文: %s = %s (旧值: %s)", key, value, oldValue);
        }
    }
    
    /**
     * 获取上下文变量
     * 优先级：线程本地 > 全局 > 系统属性
     * 
     * @param key 变量名
     * @return 变量值，如果不存在返回null
     */
    public static Object getContext(String key) {
        if (key == null) {
            return null;
        }
        
        // 1. 优先从线程本地上下文获取
        Map<String, Object> threadContext = THREAD_CONTEXT.get();
        Object value = threadContext.get(key);
        if (value != null) {
            if (debugMode) {
                logger.debug("从线程上下文获取: %s = %s", key, value);
            }
            return value;
        }
        
        // 2. 从全局上下文获取
        value = GLOBAL_CONTEXT.get(key);
        if (value != null) {
            if (debugMode) {
                logger.debug("从全局上下文获取: %s = %s", key, value);
            }
            return value;
        }
        
        // 3. 从系统属性获取
        value = System.getProperty(key);
        if (value != null) {
            if (debugMode) {
                logger.debug("从系统属性获取: %s = %s", key, value);
            }
            return value;
        }
        
        if (debugMode) {
            logger.debug("上下文变量未找到: %s", key);
        }
        
        return null;
    }
    
    /**
     * 批量设置全局上下文
     * 
     * @param contexts 上下文变量映射
     */
    public static void setGlobalContexts(Map<String, Object> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : contexts.entrySet()) {
            setGlobalContext(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 批量设置线程本地上下文
     * 
     * @param contexts 上下文变量映射
     */
    public static void setThreadContexts(Map<String, Object> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : contexts.entrySet()) {
            setThreadContext(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 移除全局上下文变量
     * 
     * @param key 变量名
     * @return 被移除的值
     */
    public static Object removeGlobalContext(String key) {
        Object value = GLOBAL_CONTEXT.remove(key);
        if (debugMode && value != null) {
            logger.debug("移除全局上下文: %s = %s", key, value);
        }
        return value;
    }
    
    /**
     * 移除线程本地上下文变量
     * 
     * @param key 变量名
     * @return 被移除的值
     */
    public static Object removeThreadContext(String key) {
        Map<String, Object> threadContext = THREAD_CONTEXT.get();
        Object value = threadContext.remove(key);
        if (debugMode && value != null) {
            logger.debug("移除线程上下文: %s = %s", key, value);
        }
        return value;
    }
    
    /**
     * 清空线程本地上下文
     */
    public static void clearThreadContext() {
        Map<String, Object> threadContext = THREAD_CONTEXT.get();
        if (debugMode && !threadContext.isEmpty()) {
            logger.debug("清空线程上下文，包含 %d 个变量", threadContext.size());
        }
        threadContext.clear();
    }
    
    /**
     * 清空全局上下文
     */
    public static void clearGlobalContext() {
        if (debugMode && !GLOBAL_CONTEXT.isEmpty()) {
            logger.debug("清空全局上下文，包含 %d 个变量", GLOBAL_CONTEXT.size());
        }
        GLOBAL_CONTEXT.clear();
    }
    
    /**
     * 获取全局上下文的副本
     * 
     * @return 全局上下文副本
     */
    public static Map<String, Object> getGlobalContextCopy() {
        return new ConcurrentHashMap<>(GLOBAL_CONTEXT);
    }
    
    /**
     * 获取线程本地上下文的副本
     * 
     * @return 线程本地上下文副本
     */
    public static Map<String, Object> getThreadContextCopy() {
        return new ConcurrentHashMap<>(THREAD_CONTEXT.get());
    }
    
    /**
     * 启用或禁用调试模式
     * 
     * @param enabled 是否启用调试模式
     */
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        logger.info("监控上下文调试模式: %s", enabled ? "启用" : "禁用");
    }
    
    /**
     * 检查是否启用了调试模式
     * 
     * @return 如果启用了调试模式返回true
     */
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * 转储当前上下文信息（用于调试）
     */
    public static void dumpContext() {
        logger.info("=== 监控上下文转储 ===");
        logger.info("全局上下文 (%d 项):", GLOBAL_CONTEXT.size());
        for (Map.Entry<String, Object> entry : GLOBAL_CONTEXT.entrySet()) {
            logger.info("  %s = %s", entry.getKey(), entry.getValue());
        }
        
        Map<String, Object> threadContext = THREAD_CONTEXT.get();
        logger.info("线程上下文 (%d 项):", threadContext.size());
        for (Map.Entry<String, Object> entry : threadContext.entrySet()) {
            logger.info("  %s = %s", entry.getKey(), entry.getValue());
        }
        logger.info("=== 上下文转储结束 ===");
    }
}