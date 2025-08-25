package com.yueny.stars.netty.monitor.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Netty监控注解
 * 用于标记需要监控的Netty服务器或客户端
 * 支持动态应用名称模板，如：${username}、${server.port}、${getClientName()}等
 * 
 * @author fengyang
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NettyMonitor {
    
    /**
     * 应用名称，默认使用类名
     * 向后兼容性：如果设置了value，则优先使用value
     */
    String value() default "";
    
    /**
     * 应用名称模板，支持变量替换
     * 支持的语法：
     * - ${variable} - 从上下文或系统属性获取变量
     * - ${variable:default} - 带默认值的变量
     * - ${methodName()} - 调用对象方法获取值
     * - ${field.methodName()} - 调用字段对象的方法
     * - ${env.VARIABLE_NAME} - 获取环境变量
     * - ${system.property} - 获取系统属性
     * 
     * 例如: "ChatsClient-${username}", "Server-${server.port:8080}"
     */
    String applicationName() default "${class.simpleName}";
    
    /**
     * 监控服务器地址模板，支持变量替换
     * 例如: "${monitor.host:localhost}"
     */
    String host() default "${monitor.host:localhost}";
    
    /**
     * 监控服务器端口，默认19999
     */
    int port() default 19999;
    
    /**
     * 是否启用监控，默认true
     */
    boolean enabled() default true;
    
    /**
     * 是否启用延迟初始化
     * 如果为true，监控初始化将延迟到所有必要的上下文信息可用时
     */
    boolean lazyInit() default true;
    
    /**
     * 初始化超时时间（毫秒），默认5000ms
     * 用于延迟初始化的超时控制
     */
    int initTimeout() default 5000;
    
    /**
     * 连接超时时间（毫秒），默认5000ms
     */
    int connectTimeout() default 5000;
    
    /**
     * 重连间隔（秒），默认5秒
     */
    int reconnectInterval() default 5;
    
    /**
     * 初始化重试次数，默认3次
     * 当初始化失败时的重试次数
     */
    int retryCount() default 3;
    
    /**
     * 重试间隔（毫秒），默认1000ms
     * 初始化失败时的重试间隔
     */
    int retryInterval() default 1000;
}