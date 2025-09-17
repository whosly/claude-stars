package com.yueny.stars.netty.monitor.agent.processor;

import com.yueny.stars.netty.monitor.agent.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * 重试和错误处理器
 * 提供智能的重试策略和错误分类处理
 * 
 * @author fengyang
 */
public class RetryErrorHandler {
    
    private static final Logger logger = Logger.getLogger(RetryErrorHandler.class);
    
    // 错误统计
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastErrorTimes = new ConcurrentHashMap<>();
    
    // 重试策略配置
    private volatile RetryStrategy defaultRetryStrategy = new RetryStrategy.ExponentialBackoff();
    private volatile int maxRetryAttempts = 3;
    private volatile long baseRetryInterval = 1000; // 1秒
    
    // 错误分类器
    private final Map<String, Predicate<Throwable>> errorClassifiers = new ConcurrentHashMap<>();
    
    public RetryErrorHandler() {
        initializeDefaultErrorClassifiers();
    }
    
    /**
     * 初始化默认的错误分类器
     */
    private void initializeDefaultErrorClassifiers() {
        // 网络相关错误 - 可重试
        errorClassifiers.put("network", throwable -> {
            String message = throwable.getMessage();
            return message != null && (
                message.contains("Connection refused") ||
                message.contains("Connection timeout") ||
                message.contains("Network is unreachable") ||
                throwable instanceof java.net.ConnectException ||
                throwable instanceof java.net.SocketTimeoutException
            );
        });
        
        // 临时资源不可用 - 可重试
        errorClassifiers.put("resource", throwable -> {
            String message = throwable.getMessage();
            return message != null && (
                message.contains("Resource temporarily unavailable") ||
                message.contains("Too many open files") ||
                throwable instanceof java.util.concurrent.RejectedExecutionException
            );
        });
        
        // 配置错误 - 不可重试
        errorClassifiers.put("configuration", throwable -> {
            return throwable instanceof IllegalArgumentException ||
                   throwable instanceof IllegalStateException ||
                   (throwable.getMessage() != null && 
                    throwable.getMessage().contains("configuration"));
        });
        
        // 权限错误 - 不可重试
        errorClassifiers.put("permission", throwable -> {
            return throwable instanceof SecurityException ||
                   (throwable.getMessage() != null && 
                    throwable.getMessage().toLowerCase().contains("permission"));
        });
    }
    
    /**
     * 判断错误是否可以重试
     */
    public boolean isRetryable(Throwable throwable) {
        // 配置和权限错误不可重试（优先级最高）
        if (errorClassifiers.get("configuration").test(throwable) ||
            errorClassifiers.get("permission").test(throwable)) {
            logger.debug("错误不可重试: %s - %s", throwable.getClass().getSimpleName(), throwable.getMessage());
            return false;
        }
        
        // 网络和资源错误可重试
        if (errorClassifiers.get("network").test(throwable) ||
            errorClassifiers.get("resource").test(throwable)) {
            logger.debug("错误可重试: %s - %s", throwable.getClass().getSimpleName(), throwable.getMessage());
            return true;
        }
        
        // 默认情况下，RuntimeException可重试，但要排除已经被分类为不可重试的错误
        if (throwable instanceof RuntimeException) {
            // 再次检查是否是不可重试的错误类型
            if (errorClassifiers.get("configuration").test(throwable) ||
                errorClassifiers.get("permission").test(throwable)) {
                logger.debug("RuntimeException但属于不可重试类型: %s - %s", throwable.getClass().getSimpleName(), throwable.getMessage());
                return false;
            }
            logger.debug("RuntimeException可重试: %s - %s", throwable.getClass().getSimpleName(), throwable.getMessage());
            return true;
        }
        
        // 其他异常不可重试
        logger.debug("非RuntimeException不可重试: %s - %s", throwable.getClass().getSimpleName(), throwable.getMessage());
        return false;
    }
    
    /**
     * 记录错误并返回是否应该重试
     */
    public RetryDecision recordError(String context, Throwable throwable, int currentAttempt) {
        String errorKey = context + ":" + throwable.getClass().getSimpleName();
        
        // 更新错误统计
        errorCounts.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();
        lastErrorTimes.put(errorKey, System.currentTimeMillis());
        
        // 记录详细错误日志
        logger.error("监控初始化错误 [%s] 第%d次尝试: %s", 
                context, currentAttempt, throwable.getMessage(), throwable);
        
        // 判断是否可以重试
        if (!isRetryable(throwable)) {
            logger.warn("错误不可重试，停止重试: %s", throwable.getMessage());
            return new RetryDecision(false, 0, "错误不可重试: " + throwable.getMessage());
        }
        
        // 检查重试次数限制
        if (currentAttempt >= maxRetryAttempts) {
            logger.warn("已达到最大重试次数 %d，停止重试", maxRetryAttempts);
            return new RetryDecision(false, 0, "已达到最大重试次数: " + maxRetryAttempts);
        }
        
        // 计算重试延迟
        long delay = defaultRetryStrategy.calculateDelay(currentAttempt + 1, baseRetryInterval);
        
        logger.info("将在 %d ms 后进行第 %d 次重试", delay, currentAttempt + 1);
        return new RetryDecision(true, delay, "准备重试");
    }
    
    /**
     * 获取错误统计信息
     */
    public ErrorStatistics getErrorStatistics(String context) {
        long totalErrors = errorCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(context + ":"))
                .mapToLong(entry -> entry.getValue().get())
                .sum();
        
        Long lastErrorTime = lastErrorTimes.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(context + ":"))
                .mapToLong(Map.Entry::getValue)
                .max()
                .orElse(0L);
        
        return new ErrorStatistics(totalErrors, lastErrorTime);
    }
    
    /**
     * 清理过期的错误统计
     */
    public void cleanupExpiredStats(long maxAge) {
        long cutoffTime = System.currentTimeMillis() - maxAge;
        
        lastErrorTimes.entrySet().removeIf(entry -> {
            if (entry.getValue() < cutoffTime) {
                errorCounts.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    // 配置方法
    public void setRetryStrategy(RetryStrategy retryStrategy) {
        this.defaultRetryStrategy = retryStrategy;
        logger.info("重试策略已更新为: %s", retryStrategy.getName());
    }
    
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        logger.info("最大重试次数已更新为: %d", this.maxRetryAttempts);
    }
    
    public void setBaseRetryInterval(long baseRetryInterval) {
        this.baseRetryInterval = Math.max(100, baseRetryInterval);
        logger.info("基础重试间隔已更新为: %d ms", this.baseRetryInterval);
    }
    
    /**
     * 添加自定义错误分类器
     */
    public void addErrorClassifier(String name, Predicate<Throwable> classifier) {
        errorClassifiers.put(name, classifier);
        logger.info("已添加错误分类器: %s", name);
    }
    
    /**
     * 重试决策结果
     */
    public static class RetryDecision {
        private final boolean shouldRetry;
        private final long delayMs;
        private final String reason;
        
        public RetryDecision(boolean shouldRetry, long delayMs, String reason) {
            this.shouldRetry = shouldRetry;
            this.delayMs = delayMs;
            this.reason = reason;
        }
        
        public boolean shouldRetry() { return shouldRetry; }
        public long getDelayMs() { return delayMs; }
        public String getReason() { return reason; }
        
        @Override
        public String toString() {
            return String.format("RetryDecision{shouldRetry=%b, delayMs=%d, reason='%s'}", 
                    shouldRetry, delayMs, reason);
        }
    }
    
    /**
     * 错误统计信息
     */
    public static class ErrorStatistics {
        private final long totalErrors;
        private final long lastErrorTime;
        
        public ErrorStatistics(long totalErrors, long lastErrorTime) {
            this.totalErrors = totalErrors;
            this.lastErrorTime = lastErrorTime;
        }
        
        public long getTotalErrors() { return totalErrors; }
        public long getLastErrorTime() { return lastErrorTime; }
        
        @Override
        public String toString() {
            return String.format("ErrorStatistics{totalErrors=%d, lastErrorTime=%d}", 
                    totalErrors, lastErrorTime);
        }
    }
}