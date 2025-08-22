package com.yueny.stars.netty.monitor.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Netty监控注解
 * 用于标记需要监控的Netty服务器或客户端
 * 
 * @author fengyang
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NettyMonitor {
    
    /**
     * 应用名称，默认使用类名
     */
    String value() default "";
    
    /**
     * 应用名称（别名）
     */
    String applicationName() default "";
    
    /**
     * 监控服务器地址，默认localhost
     */
    String host() default "localhost";
    
    /**
     * 监控服务器端口，默认19999
     */
    int port() default 19999;
    
    /**
     * 是否启用监控，默认true
     */
    boolean enabled() default true;
    
    /**
     * 连接超时时间（毫秒），默认5000ms
     */
    int connectTimeout() default 5000;
    
    /**
     * 重连间隔（秒），默认5秒
     */
    int reconnectInterval() default 5;
}