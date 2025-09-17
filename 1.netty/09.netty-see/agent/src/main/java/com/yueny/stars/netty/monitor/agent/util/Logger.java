package com.yueny.stars.netty.monitor.agent.util;

import org.slf4j.LoggerFactory;

/**
 * 日志工具类 - SLF4J代理实现
 * 
 * @author fengyang
 */
public class Logger {
    
    private final org.slf4j.Logger slf4jLogger;
    
    private Logger(org.slf4j.Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }
    
    /**
     * 根据类获取Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(LoggerFactory.getLogger(clazz));
    }
    
    /**
     * 根据名称获取Logger实例
     */
    public static Logger getLogger(String name) {
        return new Logger(LoggerFactory.getLogger(name));
    }
    
    /**
     * 记录INFO级别日志
     */
    public void info(String message, Object... args) {
        if (slf4jLogger.isInfoEnabled() && isValidMessage(message)) {
            slf4jLogger.info(formatMessage(message, args));
        }
    }
    
    /**
     * 记录TRACE级别日志
     */
    public void trace(String message, Object... args) {
        if (slf4jLogger.isTraceEnabled() && isValidMessage(message)) {
            slf4jLogger.trace(formatMessage(message, args));
        }
    }
    
    /**
     * 记录DEBUG级别日志
     */
    public void debug(String message, Object... args) {
        if (slf4jLogger.isDebugEnabled() && isValidMessage(message)) {
            slf4jLogger.debug(formatMessage(message, args));
        }
    }
    
    /**
     * 记录WARN级别日志
     */
    public void warn(String message, Object... args) {
        if (slf4jLogger.isWarnEnabled() && isValidMessage(message)) {
            slf4jLogger.warn(formatMessage(message, args));
        }
    }
    
    /**
     * 记录ERROR级别日志
     */
    public void error(String message, Object... args) {
        if (slf4jLogger.isErrorEnabled() && isValidMessage(message)) {
            slf4jLogger.error(formatMessage(message, args));
        }
    }
    
    /**
     * 记录ERROR级别日志（带异常）
     */
    public void error(String message, Throwable throwable, Object... args) {
        if (slf4jLogger.isErrorEnabled() && isValidMessage(message)) {
            slf4jLogger.error(formatMessage(message, args), throwable);
        }
    }
    
    /**
     * 检查是否启用TRACE级别
     */
    public boolean isTraceEnabled() {
        return slf4jLogger.isTraceEnabled();
    }
    
    /**
     * 检查是否启用DEBUG级别
     */
    public boolean isDebugEnabled() {
        return slf4jLogger.isDebugEnabled();
    }
    
    /**
     * 检查是否启用INFO级别
     */
    public boolean isInfoEnabled() {
        return slf4jLogger.isInfoEnabled();
    }
    
    /**
     * 检查是否启用WARN级别
     */
    public boolean isWarnEnabled() {
        return slf4jLogger.isWarnEnabled();
    }
    
    /**
     * 检查是否启用ERROR级别
     */
    public boolean isErrorEnabled() {
        return slf4jLogger.isErrorEnabled();
    }
    
    /**
     * 格式化消息（兼容原有的String.format风格）
     */
    private String formatMessage(String message, Object... args) {
        // 处理空消息
        if (message == null || message.trim().isEmpty()) {
            return "[空消息]";
        }
        
        if (args == null || args.length == 0) {
            return message;
        }
        
        try {
            return String.format(message, args);
        } catch (Exception e) {
            // 如果格式化失败，返回原始消息
            return message + " [格式化失败: " + e.getMessage() + "]";
        }
    }
    
    /**
     * 检查消息是否有效（非空且非空白）
     */
    private boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty();
    }
}