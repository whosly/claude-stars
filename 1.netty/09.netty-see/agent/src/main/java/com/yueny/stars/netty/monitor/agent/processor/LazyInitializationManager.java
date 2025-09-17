package com.yueny.stars.netty.monitor.agent.processor;

import com.yueny.stars.netty.monitor.agent.annotation.NettyMonitor;
import com.yueny.stars.netty.monitor.agent.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 延迟初始化管理器
 * 负责管理延迟初始化任务的生命周期，包括任务调度、超时处理和重试机制
 * 
 * @author fengyang
 */
public class LazyInitializationManager {
    
    private static final Logger logger = Logger.getLogger(LazyInitializationManager.class);
    
    // 待处理的初始化任务
    private final Map<String, PendingInitialization> pendingTasks = new ConcurrentHashMap<>();
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler;
    
    // 任务处理器
    private final Function<PendingInitialization, InitializationResult> taskProcessor;
    
    // 统计信息
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong successfulTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final AtomicLong timeoutTasks = new AtomicLong(0);
    
    // 配置参数
    private volatile long checkInterval = 1000; // 检查间隔（毫秒）
    private volatile boolean enabled = true;
    
    /**
     * 构造函数
     * 
     * @param taskProcessor 任务处理器
     */
    public LazyInitializationManager(Function<PendingInitialization, InitializationResult> taskProcessor) {
        this.taskProcessor = taskProcessor;
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "LazyInit-Manager");
            t.setDaemon(true);
            return t;
        });
        
        startPeriodicCheck();
        logger.info("延迟初始化管理器启动，检查间隔: %d ms", checkInterval);
    }
    
    /**
     * 添加延迟初始化任务
     * 
     * @param taskId 任务ID
     * @param clazz 目标类
     * @param annotation 注解配置
     * @return 是否成功添加
     */
    public boolean addTask(String taskId, Class<?> clazz, NettyMonitor annotation) {
        if (taskId == null || clazz == null || annotation == null) {
            logger.warn("添加延迟初始化任务失败：参数不能为null");
            return false;
        }
        
        if (!enabled) {
            logger.warn("延迟初始化管理器已禁用，拒绝添加任务: %s", taskId);
            return false;
        }
        
        // 检查任务是否已存在
        if (pendingTasks.containsKey(taskId)) {
            logger.debug("任务 %s 已存在，跳过添加", taskId);
            return false;
        }
        
        // 创建待处理任务
        PendingInitialization task = new PendingInitialization(
            taskId,
            clazz,
            annotation,
            System.currentTimeMillis(),
            annotation.initTimeout(),
            annotation.retryCount(),
            annotation.retryInterval()
        );
        
        pendingTasks.put(taskId, task);
        totalTasks.incrementAndGet();
        
        logger.info("添加延迟初始化任务: %s, 超时时间: %d ms, 最大重试: %d 次", 
                taskId, annotation.initTimeout(), annotation.retryCount());
        
        return true;
    }
    
    /**
     * 移除任务
     * 
     * @param taskId 任务ID
     * @return 被移除的任务，如果不存在返回null
     */
    public PendingInitialization removeTask(String taskId) {
        PendingInitialization task = pendingTasks.remove(taskId);
        if (task != null) {
            logger.debug("移除延迟初始化任务: %s", taskId);
        }
        return task;
    }
    
    /**
     * 获取任务
     * 
     * @param taskId 任务ID
     * @return 任务信息，如果不存在返回null
     */
    public PendingInitialization getTask(String taskId) {
        return pendingTasks.get(taskId);
    }
    
    /**
     * 手动触发任务处理
     * 
     * @param taskId 任务ID
     * @return 处理结果
     */
    public InitializationResult processTask(String taskId) {
        PendingInitialization task = pendingTasks.get(taskId);
        if (task == null) {
            return new InitializationResult(false, "任务不存在: " + taskId);
        }
        
        InitializationResult result = processTaskInternal(task);
        
        // 更新统计信息
        if (result.isSuccessful()) {
            successfulTasks.incrementAndGet();
            pendingTasks.remove(taskId); // 成功的任务从队列中移除
        } else {
            task.incrementRetryCount();
            if (task.getRetryCount() >= task.getMaxRetries()) {
                failedTasks.incrementAndGet();
                pendingTasks.remove(taskId); // 失败的任务从队列中移除
            }
        }
        
        return result;
    }
    
    /**
     * 启动定期检查
     */
    private void startPeriodicCheck() {
        scheduler.scheduleWithFixedDelay(
            this::processPendingTasks,
            checkInterval,
            checkInterval,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * 处理待处理任务
     */
    private void processPendingTasks() {
        if (!enabled || pendingTasks.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // 使用迭代器安全地遍历和修改Map
        pendingTasks.entrySet().removeIf(entry -> {
            String taskId = entry.getKey();
            PendingInitialization task = entry.getValue();
            
            // 检查是否超时
            if (currentTime - task.getCreateTime() > task.getTimeout()) {
                logger.warn("任务 %s 超时，移除队列", taskId);
                timeoutTasks.incrementAndGet();
                return true; // 移除任务
            }
            
            // 尝试处理任务
            InitializationResult result = processTaskInternal(task);
            
            if (result.isSuccessful()) {
                logger.info("延迟初始化任务 %s 成功完成", taskId);
                successfulTasks.incrementAndGet();
                return true; // 移除任务
            } else {
                // 增加重试次数
                task.incrementRetryCount();
                task.setLastRetryTime(currentTime);
                
                if (task.getRetryCount() >= task.getMaxRetries()) {
                    logger.warn("任务 %s 重试次数已达上限 (%d)，移除队列", taskId, task.getMaxRetries());
                    failedTasks.incrementAndGet();
                    return true; // 移除任务
                } else {
                    logger.debug("任务 %s 处理失败，将在下次检查时重试 (重试次数: %d/%d)", 
                            taskId, task.getRetryCount(), task.getMaxRetries());
                    return false; // 保留任务
                }
            }
        });
    }
    
    /**
     * 内部任务处理逻辑
     */
    private InitializationResult processTaskInternal(PendingInitialization task) {
        try {
            // 检查重试间隔
            long currentTime = System.currentTimeMillis();
            if (task.getLastRetryTime() > 0 && 
                currentTime - task.getLastRetryTime() < task.getRetryInterval()) {
                return new InitializationResult(false, "重试间隔未到");
            }
            
            // 调用任务处理器
            return taskProcessor.apply(task);
            
        } catch (Exception e) {
            logger.warn("处理延迟初始化任务 %s 时发生异常: %s", task.getTaskId(), e.getMessage());
            return new InitializationResult(false, "处理异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取待处理任务数量
     */
    public int getPendingTaskCount() {
        return pendingTasks.size();
    }
    
    /**
     * 获取统计信息
     */
    public Statistics getStatistics() {
        return new Statistics(
            totalTasks.get(),
            successfulTasks.get(),
            failedTasks.get(),
            timeoutTasks.get(),
            pendingTasks.size()
        );
    }
    
    /**
     * 设置检查间隔
     */
    public void setCheckInterval(long intervalMs) {
        if (intervalMs > 0) {
            this.checkInterval = intervalMs;
            logger.info("更新检查间隔为: %d ms", intervalMs);
        }
    }
    
    /**
     * 启用或禁用管理器
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logger.info("延迟初始化管理器: %s", enabled ? "启用" : "禁用");
    }
    
    /**
     * 清空所有任务
     */
    public void clearAllTasks() {
        int count = pendingTasks.size();
        pendingTasks.clear();
        logger.info("清空所有延迟初始化任务，共 %d 个", count);
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        enabled = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("延迟初始化管理器已关闭");
    }
    
    /**
     * 待处理初始化任务
     */
    public static class PendingInitialization {
        private final String taskId;
        private final Class<?> clazz;
        private final NettyMonitor annotation;
        private final long createTime;
        private final long timeout;
        private final int maxRetries;
        private final long retryInterval;
        
        private int retryCount = 0;
        private long lastRetryTime = 0;
        
        public PendingInitialization(String taskId, Class<?> clazz, NettyMonitor annotation, 
                                   long createTime, long timeout, int maxRetries, long retryInterval) {
            this.taskId = taskId;
            this.clazz = clazz;
            this.annotation = annotation;
            this.createTime = createTime;
            this.timeout = timeout;
            this.maxRetries = maxRetries;
            this.retryInterval = retryInterval;
        }
        
        // Getters
        public String getTaskId() { return taskId; }
        public Class<?> getClazz() { return clazz; }
        public NettyMonitor getAnnotation() { return annotation; }
        public long getCreateTime() { return createTime; }
        public long getTimeout() { return timeout; }
        public int getMaxRetries() { return maxRetries; }
        public long getRetryInterval() { return retryInterval; }
        public int getRetryCount() { return retryCount; }
        public long getLastRetryTime() { return lastRetryTime; }
        
        // Setters
        public void incrementRetryCount() { retryCount++; }
        public void setLastRetryTime(long lastRetryTime) { this.lastRetryTime = lastRetryTime; }
        
        @Override
        public String toString() {
            return "PendingInitialization{" +
                    "taskId='" + taskId + '\'' +
                    ", clazz=" + clazz.getSimpleName() +
                    ", retryCount=" + retryCount +
                    ", maxRetries=" + maxRetries +
                    '}';
        }
    }
    
    /**
     * 初始化结果
     */
    public static class InitializationResult {
        private final boolean successful;
        private final String message;
        private final String applicationName;
        
        public InitializationResult(boolean successful, String message) {
            this(successful, message, null);
        }
        
        public InitializationResult(boolean successful, String message, String applicationName) {
            this.successful = successful;
            this.message = message;
            this.applicationName = applicationName;
        }
        
        public boolean isSuccessful() { return successful; }
        public String getMessage() { return message; }
        public String getApplicationName() { return applicationName; }
        
        @Override
        public String toString() {
            return "InitializationResult{" +
                    "successful=" + successful +
                    ", message='" + message + '\'' +
                    ", applicationName='" + applicationName + '\'' +
                    '}';
        }
    }
    
    /**
     * 统计信息
     */
    public static class Statistics {
        private final long totalTasks;
        private final long successfulTasks;
        private final long failedTasks;
        private final long timeoutTasks;
        private final int pendingTasks;
        
        public Statistics(long totalTasks, long successfulTasks, long failedTasks, 
                         long timeoutTasks, int pendingTasks) {
            this.totalTasks = totalTasks;
            this.successfulTasks = successfulTasks;
            this.failedTasks = failedTasks;
            this.timeoutTasks = timeoutTasks;
            this.pendingTasks = pendingTasks;
        }
        
        public long getTotalTasks() { return totalTasks; }
        public long getSuccessfulTasks() { return successfulTasks; }
        public long getFailedTasks() { return failedTasks; }
        public long getTimeoutTasks() { return timeoutTasks; }
        public int getPendingTasks() { return pendingTasks; }
        
        public double getSuccessRate() {
            return totalTasks > 0 ? (double) successfulTasks / totalTasks * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("Statistics{total=%d, successful=%d, failed=%d, timeout=%d, pending=%d, successRate=%.2f%%}",
                    totalTasks, successfulTasks, failedTasks, timeoutTasks, pendingTasks, getSuccessRate());
        }
    }
}