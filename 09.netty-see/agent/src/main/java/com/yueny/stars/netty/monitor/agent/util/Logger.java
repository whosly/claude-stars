package com.yueny.stars.netty.monitor.agent.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单的日志工具类，不依赖外部库
 * 
 * @author fengyang
 */
public class Logger {
    
    private final String name;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private Logger(String name) {
        this.name = name;
    }
    
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }
    
    public static Logger getLogger(String name) {
        return new Logger(name);
    }
    
    public void info(String message, Object... args) {
        log("INFO", message, args);
    }
    
    public void debug(String message, Object... args) {
        log("DEBUG", message, args);
    }
    
    public void warn(String message, Object... args) {
        log("WARN", message, args);
    }
    
    public void error(String message, Object... args) {
        log("ERROR", message, args);
    }
    
    public void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
    }
    
    private void log(String level, String message, Object... args) {
        String timestamp = DATE_FORMAT.format(new Date());
        String formattedMessage = String.format(message, args);
        System.out.println(String.format("[%s] %s [%s] %s", timestamp, level, name, formattedMessage));
    }
}