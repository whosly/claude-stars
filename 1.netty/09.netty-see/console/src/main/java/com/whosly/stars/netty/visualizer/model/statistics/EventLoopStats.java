package com.whosly.stars.netty.visualizer.model.statistics;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * EventLoop维度统计信息
 * 
 * @author fengyang
 */
@Data
public class EventLoopStats {
    
    private String eventLoopName;
    private String eventLoopType;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    
    // 基本统计
    private final AtomicLong totalChannels = new AtomicLong(0);
    private final AtomicLong currentChannels = new AtomicLong(0);
    private final AtomicLong peakChannels = new AtomicLong(0);
    
    // 任务执行统计
    private final AtomicLong totalTasksExecuted = new AtomicLong(0);
    private final AtomicLong totalTasksQueued = new AtomicLong(0);
    private final AtomicLong currentQueueSize = new AtomicLong(0);
    private final AtomicLong peakQueueSize = new AtomicLong(0);
    
    // 执行时间统计
    private final AtomicReference<Double> avgTaskExecutionTime = new AtomicReference<>(0.0);
    private final AtomicReference<Double> minTaskExecutionTime = new AtomicReference<>(Double.MAX_VALUE);
    private final AtomicReference<Double> maxTaskExecutionTime = new AtomicReference<>(0.0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    // 负载统计
    private final AtomicReference<Double> cpuUsage = new AtomicReference<>(0.0);
    private final AtomicReference<Double> avgLoad = new AtomicReference<>(0.0);
    private final AtomicReference<Double> peakLoad = new AtomicReference<>(0.0);
    
    // 数据传输统计
    private final AtomicLong totalBytesProcessed = new AtomicLong(0);
    private final AtomicLong totalMessagesProcessed = new AtomicLong(0);
    private final AtomicReference<Double> avgThroughput = new AtomicReference<>(0.0);
    
    // 错误统计
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong taskExecutionErrors = new AtomicLong(0);
    private final AtomicLong channelErrors = new AtomicLong(0);
    private final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();
    
    // Channel分布统计
    private final Map<String, AtomicLong> channelsByApplication = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> channelsByType = new ConcurrentHashMap<>();
    
    // 活跃Channel列表
    private final Set<String> activeChannels = new ConcurrentSkipListSet<>();
    
    // 负载均衡统计
    private final AtomicReference<Double> loadBalanceScore = new AtomicReference<>(1.0);
    private final AtomicLong rebalanceCount = new AtomicLong(0);
    
    // 时间分布统计
    private final Map<Integer, AtomicLong> tasksByHour = new ConcurrentHashMap<>();
    
    public EventLoopStats(String eventLoopName, String eventLoopType) {
        this.eventLoopName = eventLoopName;
        this.eventLoopType = eventLoopType;
        this.firstSeen = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
        
        // 初始化24小时统计
        for (int i = 0; i < 24; i++) {
            tasksByHour.put(i, new AtomicLong(0));
        }
    }
    
    /**
     * 记录新Channel注册
     */
    public void recordChannelRegistered(String channelId, String application, String channelType) {
        totalChannels.incrementAndGet();
        long current = currentChannels.incrementAndGet();
        peakChannels.updateAndGet(peak -> Math.max(peak, current));
        
        activeChannels.add(channelId);
        
        // 记录应用分布
        channelsByApplication.computeIfAbsent(application, k -> new AtomicLong(0)).incrementAndGet();
        
        // 记录Channel类型分布
        channelsByType.computeIfAbsent(channelType, k -> new AtomicLong(0)).incrementAndGet();
        
        updateLastSeen();
    }
    
    /**
     * 记录Channel注销
     */
    public void recordChannelUnregistered(String channelId, String application, String channelType) {
        long current = currentChannels.decrementAndGet();
        if (current < 0) {
            currentChannels.set(0);
        }
        
        activeChannels.remove(channelId);
        
        // 更新应用分布
        AtomicLong appCount = channelsByApplication.get(application);
        if (appCount != null && appCount.get() > 0) {
            appCount.decrementAndGet();
        }
        
        // 更新Channel类型分布
        AtomicLong typeCount = channelsByType.get(channelType);
        if (typeCount != null && typeCount.get() > 0) {
            typeCount.decrementAndGet();
        }
        
        updateLastSeen();
    }
    
    /**
     * 记录任务执行
     */
    public void recordTaskExecution(double executionTimeMs, boolean success) {
        totalTasksExecuted.incrementAndGet();
        totalExecutionTime.addAndGet((long) executionTimeMs);
        
        // 更新执行时间统计
        updateExecutionTimeStats(executionTimeMs);
        
        if (!success) {
            taskExecutionErrors.incrementAndGet();
            totalErrors.incrementAndGet();
        }
        
        // 记录时间分布
        int hour = LocalDateTime.now().getHour();
        tasksByHour.get(hour).incrementAndGet();
        
        updateLastSeen();
    }
    
    /**
     * 记录任务入队
     */
    public void recordTaskQueued() {
        totalTasksQueued.incrementAndGet();
        long current = currentQueueSize.incrementAndGet();
        peakQueueSize.updateAndGet(peak -> Math.max(peak, current));
        updateLastSeen();
    }
    
    /**
     * 记录任务出队
     */
    public void recordTaskDequeued() {
        long current = currentQueueSize.decrementAndGet();
        if (current < 0) {
            currentQueueSize.set(0);
        }
        updateLastSeen();
    }
    
    /**
     * 更新队列大小
     */
    public void updateQueueSize(long size) {
        currentQueueSize.set(size);
        peakQueueSize.updateAndGet(peak -> Math.max(peak, size));
        updateLastSeen();
    }
    
    /**
     * 记录数据处理
     */
    public void recordDataProcessed(long bytes, int messages) {
        totalBytesProcessed.addAndGet(bytes);
        totalMessagesProcessed.addAndGet(messages);
        
        // 计算吞吐量
        calculateThroughput();
        updateLastSeen();
    }
    
    /**
     * 记录Channel错误
     */
    public void recordChannelError(String errorType) {
        channelErrors.incrementAndGet();
        totalErrors.incrementAndGet();
        errorsByType.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
        updateLastSeen();
    }
    
    /**
     * 更新CPU使用率
     */
    public void updateCpuUsage(double usage) {
        cpuUsage.set(usage);
        updateLoad();
        updateLastSeen();
    }
    
    /**
     * 更新负载均衡分数
     */
    public void updateLoadBalanceScore(double score) {
        double oldScore = loadBalanceScore.get();
        loadBalanceScore.set(score);
        
        // 如果负载均衡分数显著变化，记录重平衡事件
        if (Math.abs(score - oldScore) > 0.2) {
            rebalanceCount.incrementAndGet();
        }
        
        updateLastSeen();
    }
    
    /**
     * 更新执行时间统计
     */
    private void updateExecutionTimeStats(double executionTimeMs) {
        minTaskExecutionTime.updateAndGet(min -> Math.min(min, executionTimeMs));
        maxTaskExecutionTime.updateAndGet(max -> Math.max(max, executionTimeMs));
        
        // 计算平均执行时间
        long tasks = totalTasksExecuted.get();
        if (tasks > 0) {
            double avgTime = (double) totalExecutionTime.get() / tasks;
            avgTaskExecutionTime.set(avgTime);
        }
    }
    
    /**
     * 计算吞吐量
     */
    private void calculateThroughput() {
        long duration = java.time.Duration.between(firstSeen, lastSeen).getSeconds();
        if (duration > 0) {
            double throughput = (double) totalBytesProcessed.get() / duration;
            avgThroughput.set(throughput);
        }
    }
    
    /**
     * 更新负载指标
     */
    private void updateLoad() {
        // 基于队列大小、CPU使用率和Channel数量计算负载
        double queueLoad = Math.min(1.0, (double) currentQueueSize.get() / 1000); // 假设1000为满负载
        double channelLoad = Math.min(1.0, (double) currentChannels.get() / 10000); // 假设10000为满负载
        double cpuLoad = cpuUsage.get() / 100.0;
        
        double currentLoad = (queueLoad + channelLoad + cpuLoad) / 3.0;
        avgLoad.set(currentLoad);
        peakLoad.updateAndGet(peak -> Math.max(peak, currentLoad));
    }
    
    /**
     * 更新最后活跃时间
     */
    private void updateLastSeen() {
        lastSeen = LocalDateTime.now();
    }
    
    /**
     * 获取错误率
     */
    public double getErrorRate() {
        long total = totalTasksExecuted.get();
        if (total > 0) {
            return (double) totalErrors.get() / total * 100;
        }
        return 0.0;
    }
    
    /**
     * 获取平均队列等待时间 (估算)
     */
    public double getAvgQueueWaitTime() {
        long queued = totalTasksQueued.get();
        long executed = totalTasksExecuted.get();
        if (executed > 0 && queued > 0) {
            // 简化估算：基于队列大小和执行时间
            return (double) currentQueueSize.get() * avgTaskExecutionTime.get();
        }
        return 0.0;
    }
    
    /**
     * 获取最活跃的应用
     */
    public String getMostActiveApplication() {
        return channelsByApplication.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * 获取最常见的Channel类型
     */
    public String getMostCommonChannelType() {
        return channelsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * 获取最常见的错误类型
     */
    public String getMostCommonErrorType() {
        return errorsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("None");
    }
    
    /**
     * 获取最繁忙的小时
     */
    public int getBusiestHour() {
        return tasksByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse(0);
    }
    
    /**
     * 检查是否负载过高
     */
    public boolean isOverloaded() {
        return avgLoad.get() > 0.8 || currentQueueSize.get() > 500 || cpuUsage.get() > 90;
    }
    
    /**
     * 检查是否需要重平衡
     */
    public boolean needsRebalancing() {
        return loadBalanceScore.get() < 0.5 || isOverloaded();
    }
    
    /**
     * 重置统计数据
     */
    public void reset() {
        totalChannels.set(0);
        currentChannels.set(0);
        peakChannels.set(0);
        
        totalTasksExecuted.set(0);
        totalTasksQueued.set(0);
        currentQueueSize.set(0);
        peakQueueSize.set(0);
        
        avgTaskExecutionTime.set(0.0);
        minTaskExecutionTime.set(Double.MAX_VALUE);
        maxTaskExecutionTime.set(0.0);
        totalExecutionTime.set(0);
        
        cpuUsage.set(0.0);
        avgLoad.set(0.0);
        peakLoad.set(0.0);
        
        totalBytesProcessed.set(0);
        totalMessagesProcessed.set(0);
        avgThroughput.set(0.0);
        
        totalErrors.set(0);
        taskExecutionErrors.set(0);
        channelErrors.set(0);
        errorsByType.clear();
        
        channelsByApplication.clear();
        channelsByType.clear();
        activeChannels.clear();
        
        loadBalanceScore.set(1.0);
        rebalanceCount.set(0);
        
        tasksByHour.values().forEach(counter -> counter.set(0));
        
        firstSeen = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
    }
    
    /**
     * 获取统计摘要
     */
    public EventLoopStatsSummary getSummary() {
        calculateThroughput();
        
        return EventLoopStatsSummary.builder()
                .eventLoopName(eventLoopName)
                .eventLoopType(eventLoopType)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .totalChannels(totalChannels.get())
                .currentChannels(currentChannels.get())
                .peakChannels(peakChannels.get())
                .totalTasksExecuted(totalTasksExecuted.get())
                .currentQueueSize(currentQueueSize.get())
                .peakQueueSize(peakQueueSize.get())
                .avgTaskExecutionTime(avgTaskExecutionTime.get())
                .cpuUsage(cpuUsage.get())
                .avgLoad(avgLoad.get())
                .errorRate(getErrorRate())
                .avgThroughput(avgThroughput.get())
                .loadBalanceScore(loadBalanceScore.get())
                .isOverloaded(isOverloaded())
                .needsRebalancing(needsRebalancing())
                .mostActiveApplication(getMostActiveApplication())
                .mostCommonChannelType(getMostCommonChannelType())
                .mostCommonErrorType(getMostCommonErrorType())
                .busiestHour(getBusiestHour())
                .build();
    }
    
    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class EventLoopStatsSummary {
        private String eventLoopName;
        private String eventLoopType;
        private LocalDateTime firstSeen;
        private LocalDateTime lastSeen;
        private long totalChannels;
        private long currentChannels;
        private long peakChannels;
        private long totalTasksExecuted;
        private long currentQueueSize;
        private long peakQueueSize;
        private double avgTaskExecutionTime;
        private double cpuUsage;
        private double avgLoad;
        private double errorRate;
        private double avgThroughput;
        private double loadBalanceScore;
        private boolean isOverloaded;
        private boolean needsRebalancing;
        private String mostActiveApplication;
        private String mostCommonChannelType;
        private String mostCommonErrorType;
        private int busiestHour;
    }
}