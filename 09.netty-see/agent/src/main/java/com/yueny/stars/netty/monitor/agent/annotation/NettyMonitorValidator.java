package com.yueny.stars.netty.monitor.agent.annotation;

import com.yueny.stars.netty.monitor.agent.template.TemplateResolver;
import com.yueny.stars.netty.monitor.agent.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * NettyMonitor注解验证器
 * 用于验证注解属性的有效性，包括模板语法验证
 * 
 * @author fengyang
 */
public class NettyMonitorValidator {
    
    private static final Logger logger = Logger.getLogger(NettyMonitorValidator.class);
    
    private final TemplateResolver templateResolver;
    
    public NettyMonitorValidator(TemplateResolver templateResolver) {
        this.templateResolver = templateResolver;
    }
    
    /**
     * 验证NettyMonitor注解配置
     * 
     * @param annotation NettyMonitor注解实例
     * @return 验证结果
     */
    public ValidationResult validate(NettyMonitor annotation) {
        if (annotation == null) {
            return new ValidationResult(false, "注解不能为null");
        }
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 验证应用名称
        validateApplicationName(annotation, errors, warnings);
        
        // 验证主机地址
        validateHost(annotation, errors, warnings);
        
        // 验证端口
        validatePort(annotation, errors, warnings);
        
        // 验证超时配置
        validateTimeouts(annotation, errors, warnings);
        
        // 验证重试配置
        validateRetryConfig(annotation, errors, warnings);
        
        boolean isValid = errors.isEmpty();
        String message = buildValidationMessage(errors, warnings);
        
        return new ValidationResult(isValid, message, warnings);
    }
    
    /**
     * 验证应用名称配置
     */
    private void validateApplicationName(NettyMonitor annotation, List<String> errors, List<String> warnings) {
        String value = annotation.value();
        String applicationName = annotation.applicationName();
        
        // 检查是否同时设置了value和applicationName
        if (!value.isEmpty() && !applicationName.equals("${class.simpleName}")) {
            warnings.add("同时设置了value和applicationName，将优先使用value");
        }
        
        // 获取实际使用的应用名称
        String actualName = !value.isEmpty() ? value : applicationName;
        
        if (actualName.isEmpty()) {
            errors.add("应用名称不能为空");
            return;
        }
        
        // 验证模板语法
        if (templateResolver != null && actualName.contains("${")) {
            TemplateResolver.ValidationResult templateResult = templateResolver.validate(actualName);
            if (!templateResult.isValid()) {
                errors.add("应用名称模板语法错误: " + templateResult.getMessage());
            } else {
                logger.debug("应用名称模板验证通过: %s", templateResult.getMessage());
            }
        }
    }
    
    /**
     * 验证主机地址配置
     */
    private void validateHost(NettyMonitor annotation, List<String> errors, List<String> warnings) {
        String host = annotation.host();
        
        if (host.isEmpty()) {
            errors.add("主机地址不能为空");
            return;
        }
        
        // 验证模板语法
        if (templateResolver != null && host.contains("${")) {
            TemplateResolver.ValidationResult templateResult = templateResolver.validate(host);
            if (!templateResult.isValid()) {
                errors.add("主机地址模板语法错误: " + templateResult.getMessage());
            } else {
                logger.debug("主机地址模板验证通过: %s", templateResult.getMessage());
            }
        }
    }
    
    /**
     * 验证端口配置
     */
    private void validatePort(NettyMonitor annotation, List<String> errors, List<String> warnings) {
        int port = annotation.port();
        
        if (port <= 0 || port > 65535) {
            errors.add("端口号必须在1-65535范围内，当前值: " + port);
        }
        
        if (port < 1024) {
            warnings.add("使用系统端口 " + port + "，可能需要管理员权限");
        }
    }
    
    /**
     * 验证超时配置
     */
    private void validateTimeouts(NettyMonitor annotation, List<String> errors, List<String> warnings) {
        int initTimeout = annotation.initTimeout();
        int connectTimeout = annotation.connectTimeout();
        
        if (initTimeout <= 0) {
            errors.add("初始化超时时间必须大于0，当前值: " + initTimeout);
        }
        
        if (connectTimeout <= 0) {
            errors.add("连接超时时间必须大于0，当前值: " + connectTimeout);
        }
        
        if (initTimeout > 60000) {
            warnings.add("初始化超时时间过长: " + initTimeout + "ms，建议不超过60秒");
        }
        
        if (connectTimeout > 30000) {
            warnings.add("连接超时时间过长: " + connectTimeout + "ms，建议不超过30秒");
        }
    }
    
    /**
     * 验证重试配置
     */
    private void validateRetryConfig(NettyMonitor annotation, List<String> errors, List<String> warnings) {
        int retryCount = annotation.retryCount();
        int retryInterval = annotation.retryInterval();
        int reconnectInterval = annotation.reconnectInterval();
        
        if (retryCount < 0) {
            errors.add("重试次数不能为负数，当前值: " + retryCount);
        }
        
        if (retryInterval <= 0) {
            errors.add("重试间隔必须大于0，当前值: " + retryInterval);
        }
        
        if (reconnectInterval <= 0) {
            errors.add("重连间隔必须大于0，当前值: " + reconnectInterval);
        }
        
        if (retryCount > 10) {
            warnings.add("重试次数过多: " + retryCount + "，建议不超过10次");
        }
        
        if (retryInterval > 10000) {
            warnings.add("重试间隔过长: " + retryInterval + "ms，建议不超过10秒");
        }
    }
    
    /**
     * 构建验证消息
     */
    private String buildValidationMessage(List<String> errors, List<String> warnings) {
        StringBuilder message = new StringBuilder();
        
        if (!errors.isEmpty()) {
            message.append("错误: ");
            message.append(String.join("; ", errors));
        }
        
        if (!warnings.isEmpty()) {
            if (message.length() > 0) {
                message.append(" ");
            }
            message.append("警告: ");
            message.append(String.join("; ", warnings));
        }
        
        if (message.length() == 0) {
            message.append("验证通过");
        }
        
        return message.toString();
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, String message) {
            this(valid, message, new ArrayList<>());
        }
        
        public ValidationResult(boolean valid, String message, List<String> warnings) {
            this.valid = valid;
            this.message = message;
            this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", message='" + message + "', warnings=" + warnings.size() + "}";
        }
    }
}