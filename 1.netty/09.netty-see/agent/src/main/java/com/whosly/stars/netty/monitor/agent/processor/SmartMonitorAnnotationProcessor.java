package com.whosly.stars.netty.monitor.agent.processor;

import com.whosly.stars.netty.monitor.agent.NettyMonitorAgent;
import com.whosly.stars.netty.monitor.agent.annotation.NettyMonitor;
import com.whosly.stars.netty.monitor.agent.annotation.NettyMonitorValidator;
import com.whosly.stars.netty.monitor.agent.template.TemplateResolver;
import com.whosly.stars.netty.monitor.agent.template.resolver.*;
import com.whosly.stars.netty.monitor.agent.util.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 智能监控注解处理器
 * 负责处理@NettyMonitor注解，支持模板解析、延迟初始化和自动重试
 * 
 * @author fengyang
 */
public class SmartMonitorAnnotationProcessor {
    
    private static final Logger logger = Logger.getLogger(SmartMonitorAnnotationProcessor.class);
    
    // 单例实例
    private static volatile SmartMonitorAnnotationProcessor instance;
    
    // 模板解析器
    private final TemplateResolver templateResolver;
    
    // 注解验证器
    private final NettyMonitorValidator validator;
    
    // 重试和错误处理器
    private final RetryErrorHandler retryErrorHandler;
    
    // 待处理的初始化任务
    private final Map<String, PendingInitialization> pendingInits = new ConcurrentHashMap<>();
    
    // 已处理的类缓存
    private final Map<String, ProcessedClass> processedClasses = new ConcurrentHashMap<>();
    
    // NoOp监控器缓存
    private final Map<String, NoOpMonitor> noOpMonitors = new ConcurrentHashMap<>();
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // 默认重试间隔（毫秒）
    private volatile long defaultRetryInterval = 5000;
    
    /**
     * 私有构造函数
     */
    private SmartMonitorAnnotationProcessor() {
        // 初始化模板解析器，按优先级添加解析器
        this.templateResolver = new TemplateResolver(Arrays.asList(
            new ContextVariableResolver(),      // 优先级: 10
            new MethodCallResolver(),           // 优先级: 20
            new EnvironmentResolver(),          // 优先级: 40
            new SystemPropertyResolver()        // 优先级: 50
        ));
        
        this.validator = new NettyMonitorValidator(templateResolver);
        this.retryErrorHandler = new RetryErrorHandler();
        
        // 启动定时重试任务
        startRetryTask();
        
        logger.info("智能监控注解处理器初始化完成");
    }
    
    /**
     * 获取单例实例
     */
    public static SmartMonitorAnnotationProcessor getInstance() {
        if (instance == null) {
            synchronized (SmartMonitorAnnotationProcessor.class) {
                if (instance == null) {
                    instance = new SmartMonitorAnnotationProcessor();
                }
            }
        }
        return instance;
    }
    
    /**
     * 处理类级别注解
     * 
     * @param clazz 目标类
     * @return 处理结果
     */
    public ProcessResult processClass(Class<?> clazz) {
        if (clazz == null) {
            return new ProcessResult(false, "类不能为null");
        }
        
        String className = clazz.getName();
        logger.debug("开始处理类: %s", className);
        
        // 检查是否已经处理过
        ProcessedClass processed = processedClasses.get(className);
        if (processed != null && processed.isSuccessful()) {
            logger.debug("类 %s 已经成功处理过，跳过", className);
            return new ProcessResult(true, "已处理", processed.getApplicationName());
        }
        
        // 获取注解
        NettyMonitor annotation = clazz.getAnnotation(NettyMonitor.class);
        if (annotation == null) {
            return new ProcessResult(false, "类上没有@NettyMonitor注解");
        }
        
        // 验证注解配置
        NettyMonitorValidator.ValidationResult validationResult = validator.validate(annotation);
        if (!validationResult.isValid()) {
            String error = "注解配置无效: " + validationResult.getMessage();
            logger.warn(error);
            return new ProcessResult(false, error);
        }
        
        // 记录警告
        if (validationResult.hasWarnings()) {
            for (String warning : validationResult.getWarnings()) {
                logger.warn("注解配置警告: %s", warning);
            }
        }
        
        try {
            // 解析应用名称
            String applicationName = resolveApplicationName(annotation, clazz);
            if (applicationName == null) {
                if (annotation.lazyInit()) {
                    // 延迟初始化
                    return processLazyInitialization(clazz, annotation);
                } else {
                    return new ProcessResult(false, "无法解析应用名称且未启用延迟初始化");
                }
            }
            
            // 解析主机地址
            String host = resolveHost(annotation, clazz);
            if (host == null) {
                host = "localhost"; // 使用默认值
            }
            
            // 执行初始化
            ProcessResult result = performInitializationWithRetry(clazz, annotation, applicationName, host);
            
            // 记录处理结果
            if (result.isSuccessful()) {
                processedClasses.put(className, new ProcessedClass(className, applicationName, true));
                logger.info("成功处理类 %s，应用名称: %s", className, applicationName);
            } else {
                processedClasses.put(className, new ProcessedClass(className, null, false));
                logger.warn("处理类 %s 失败: %s", className, result.getMessage());
                
                // 创建NoOp监控器作为降级处理
                createNoOpMonitor(className, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            String error = "处理类 " + className + " 时发生异常: " + e.getMessage();
            logger.error(error, e);
            return new ProcessResult(false, error);
        }
    }
    
    /**
     * 延迟初始化处理
     */
    public ProcessResult processLazyInitialization(Class<?> clazz, NettyMonitor annotation) {
        String className = clazz.getName();
        
        // 检查是否已经在待处理队列中
        if (pendingInits.containsKey(className)) {
            logger.debug("类 %s 已在延迟初始化队列中", className);
            return new ProcessResult(true, "已加入延迟初始化队列");
        }
        
        // 创建待处理任务
        PendingInitialization pending = new PendingInitialization(
            clazz, 
            annotation, 
            System.currentTimeMillis(),
            annotation.initTimeout(),
            annotation.retryCount()
        );
        
        pendingInits.put(className, pending);
        logger.info("类 %s 加入延迟初始化队列，超时时间: %d ms", className, annotation.initTimeout());
        
        return new ProcessResult(true, "已加入延迟初始化队列");
    }
    
    /**
     * 重试失败的初始化
     */
    public void retryFailedInitializations() {
        if (pendingInits.isEmpty()) {
            return;
        }
        
        logger.debug("开始重试失败的初始化，待处理数量: %d", pendingInits.size());
        
        long currentTime = System.currentTimeMillis();
        
        // 遍历待处理任务
        pendingInits.entrySet().removeIf(entry -> {
            String className = entry.getKey();
            PendingInitialization pending = entry.getValue();
            
            // 检查是否超时
            if (currentTime - pending.getCreateTime() > pending.getTimeout()) {
                logger.warn("类 %s 延迟初始化超时，移除队列", className);
                processedClasses.put(className, new ProcessedClass(className, null, false));
                return true;
            }
            
            // 尝试重新处理
            try {
                String applicationName = resolveApplicationName(pending.getAnnotation(), pending.getClazz());
                if (applicationName != null) {
                    String host = resolveHost(pending.getAnnotation(), pending.getClazz());
                    if (host == null) {
                        host = "localhost";
                    }
                    
                    ProcessResult result = performInitializationWithRetry(
                        pending.getClazz(), 
                        pending.getAnnotation(), 
                        applicationName, 
                        host
                    );
                    
                    if (result.isSuccessful()) {
                        logger.info("延迟初始化成功: %s -> %s", className, applicationName);
                        processedClasses.put(className, new ProcessedClass(className, applicationName, true));
                        return true; // 移除队列
                    } else {
                        pending.incrementRetryCount();
                        if (pending.getRetryCount() >= pending.getMaxRetries()) {
                            logger.warn("类 %s 重试次数已达上限，移除队列", className);
                            processedClasses.put(className, new ProcessedClass(className, null, false));
                            createNoOpMonitor(className, "重试次数已达上限");
                            return true; // 移除队列
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("重试初始化类 %s 时发生异常: %s", className, e.getMessage());
                pending.incrementRetryCount();
                if (pending.getRetryCount() >= pending.getMaxRetries()) {
                    logger.warn("类 %s 重试次数已达上限，移除队列", className);
                    processedClasses.put(className, new ProcessedClass(className, null, false));
                    createNoOpMonitor(className, "重试异常次数已达上限");
                    return true; // 移除队列
                }
            }
            
            return false; // 保留在队列中
        });
    }
    
    /**
     * 解析应用名称
     */
    private String resolveApplicationName(NettyMonitor annotation, Class<?> clazz) {
        // 优先使用value（向后兼容）
        String name = annotation.value();
        if (name.isEmpty()) {
            name = annotation.applicationName();
        }
        
        if (name.isEmpty()) {
            return null;
        }
        
        // 处理特殊变量
        if ("${class.simpleName}".equals(name)) {
            return clazz.getSimpleName();
        }
        
        // 使用模板解析器解析
        return templateResolver.resolve(name, clazz);
    }
    
    /**
     * 解析主机地址
     */
    private String resolveHost(NettyMonitor annotation, Class<?> clazz) {
        String host = annotation.host();
        if (host.isEmpty()) {
            return null;
        }
        
        // 使用模板解析器解析
        return templateResolver.resolve(host, clazz);
    }
    
    /**
     * 执行带重试的初始化
     */
    private ProcessResult performInitializationWithRetry(Class<?> clazz, NettyMonitor annotation, String applicationName, String host) {
        String context = clazz.getName();
        int maxRetries = annotation.retryCount();
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                // 执行实际初始化
                ProcessResult result = performInitialization(clazz, annotation, applicationName, host);
                if (result.isSuccessful()) {
                    if (attempt > 0) {
                        logger.info("类 %s 在第 %d 次尝试后初始化成功", context, attempt + 1);
                    }
                    return result;
                }
                
                // 初始化失败，但没有异常
                if (attempt < maxRetries) {
                    logger.warn("类 %s 第 %d 次初始化失败: %s，准备重试", context, attempt + 1, result.getMessage());
                    Thread.sleep(1000); // 简单延迟
                } else {
                    logger.error("类 %s 初始化最终失败: %s", context, result.getMessage());
                    return result;
                }
                
            } catch (Exception e) {
                // 记录错误并判断是否重试
                RetryErrorHandler.RetryDecision decision = retryErrorHandler.recordError(context, e, attempt);
                
                if (!decision.shouldRetry() || attempt >= maxRetries) {
                    logger.error("类 %s 初始化最终失败: %s", context, decision.getReason());
                    return new ProcessResult(false, "初始化失败: " + e.getMessage());
                }
                
                // 等待重试
                try {
                    Thread.sleep(decision.getDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new ProcessResult(false, "初始化被中断");
                }
            }
        }
        
        return new ProcessResult(false, "初始化失败，已达最大重试次数");
    }
    
    /**
     * 执行初始化
     */
    private ProcessResult performInitialization(Class<?> clazz, NettyMonitor annotation, String applicationName, String host) {
        try {
            logger.info("初始化监控: 应用=%s, 主机=%s, 端口=%d", applicationName, host, annotation.port());
            
            // 集成到NettyMonitorAgent
            NettyMonitorAgent.initialize(applicationName);
            logger.info("NettyMonitorAgent初始化成功: %s", applicationName);
            
            return new ProcessResult(true, "初始化成功", applicationName);
        } catch (Exception e) {
            logger.error("NettyMonitorAgent初始化失败: %s", e.getMessage());
            throw new RuntimeException("监控初始化失败", e);
        }
    }
    
    /**
     * 创建NoOp监控器作为降级处理
     */
    private void createNoOpMonitor(String className, String reason) {
        NoOpMonitor noOpMonitor = new NoOpMonitor(className, reason);
        noOpMonitors.put(className, noOpMonitor);
        logger.info("为类 %s 创建NoOp监控器: %s", className, reason);
    }
    
    /**
     * 获取NoOp监控器
     */
    public NoOpMonitor getNoOpMonitor(String className) {
        return noOpMonitors.get(className);
    }
    
    /**
     * 获取错误统计信息
     */
    public RetryErrorHandler.ErrorStatistics getErrorStatistics(String context) {
        return retryErrorHandler.getErrorStatistics(context);
    }
    
    /**
     * 启动重试任务
     */
    private void startRetryTask() {
        scheduler.scheduleWithFixedDelay(
            this::retryFailedInitializations,
            defaultRetryInterval,
            defaultRetryInterval,
            TimeUnit.MILLISECONDS
        );
        
        logger.debug("启动重试任务，间隔: %d ms", defaultRetryInterval);
    }
    
    /**
     * 获取模板解析器
     */
    public TemplateResolver getTemplateResolver() {
        return templateResolver;
    }
    
    /**
     * 获取待处理初始化数量
     */
    public int getPendingInitializationCount() {
        return pendingInits.size();
    }
    
    /**
     * 获取已处理类数量
     */
    public int getProcessedClassCount() {
        return processedClasses.size();
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        processedClasses.clear();
        pendingInits.clear();
        noOpMonitors.clear();
        templateResolver.clearCache();
        retryErrorHandler.cleanupExpiredStats(24 * 60 * 60 * 1000); // 清理24小时前的错误统计
        logger.info("清空处理器缓存");
    }
    
    /**
     * 关闭处理器
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("智能监控注解处理器已关闭");
    }
    
    /**
     * 待处理初始化任务
     */
    private static class PendingInitialization {
        private final Class<?> clazz;
        private final NettyMonitor annotation;
        private final long createTime;
        private final long timeout;
        private final int maxRetries;
        private int retryCount = 0;
        
        public PendingInitialization(Class<?> clazz, NettyMonitor annotation, long createTime, long timeout, int maxRetries) {
            this.clazz = clazz;
            this.annotation = annotation;
            this.createTime = createTime;
            this.timeout = timeout;
            this.maxRetries = maxRetries;
        }
        
        public Class<?> getClazz() { return clazz; }
        public NettyMonitor getAnnotation() { return annotation; }
        public long getCreateTime() { return createTime; }
        public long getTimeout() { return timeout; }
        public int getMaxRetries() { return maxRetries; }
        public int getRetryCount() { return retryCount; }
        
        public void incrementRetryCount() { retryCount++; }
    }
    
    /**
     * 已处理类信息
     */
    private static class ProcessedClass {
        private final String className;
        private final String applicationName;
        private final boolean successful;
        
        public ProcessedClass(String className, String applicationName, boolean successful) {
            this.className = className;
            this.applicationName = applicationName;
            this.successful = successful;
        }
        
        public String getClassName() { return className; }
        public String getApplicationName() { return applicationName; }
        public boolean isSuccessful() { return successful; }
    }
    
    /**
     * 处理结果
     */
    public static class ProcessResult {
        private final boolean successful;
        private final String message;
        private final String applicationName;
        
        public ProcessResult(boolean successful, String message) {
            this(successful, message, null);
        }
        
        public ProcessResult(boolean successful, String message, String applicationName) {
            this.successful = successful;
            this.message = message;
            this.applicationName = applicationName;
        }
        
        public boolean isSuccessful() { return successful; }
        public String getMessage() { return message; }
        public String getApplicationName() { return applicationName; }
        
        @Override
        public String toString() {
            return "ProcessResult{successful=" + successful + ", message='" + message + "', applicationName='" + applicationName + "'}";
        }
    }
}