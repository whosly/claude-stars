package com.yueny.stars.netty.visualizer.model.statistics;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 时间窗口统计信息
 * 
 * @author fengyang
 */
@Data
public class TimeWindowStats {
    
    // 时间窗口信息
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private long windowDurationSeconds;
    
    // 连接统计
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong newConnections = new AtomicLong(0);
    private final AtomicLong closedConnections = new AtomicLong(0);
    private final AtomicLong peakConnections = new AtomicLong(0);
    
    // 数据传输统计
    private final AtomicLong totalBytesRead = new AtomicLong(0);
    private final AtomicLong totalBytesWritten = new AtomicLong(0);
    private final AtomicLong totalMessagesRead = new AtomicLong(0);
    private final AtomicLong totalMessagesWritten = new AtomicLong(0);
    
    // 性能统计
    private final AtomicReference<Double> avgResponseTime = new AtomicReference<>(0.0);
    private final AtomicReference<Double> minResponseTime = new AtomicReference<>(Double.MAX_VALUE);
    private final AtomicReference<Double> maxResponseTime = new AtomicReference<>(0.0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    
    // 错误统计
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicReference<Double> errorRate = new AtomicReference<>(0.0);
    
    // 吞吐量统计
    private final AtomicReference<Double> tps = new AtomicReference<>(0.0); // Transactions Per Second
    private final AtomicReference<Double> qps = new AtomicReference<>(0.0); // Queries Per Second
    private final AtomicReference<Double> bytesPerSecond = new AtomicReference<>(0.0);
    
    // 缓冲区统计
    private final AtomicLong totalBufferAllocations = new AtomicLong(0);
    private final AtomicLong totalBufferDeallocations = new AtomicLong(0);
    private final AtomicLong peakBufferUsage = new AtomicLong(0);
    private final AtomicReference<Double> avgBufferUtilization = new AtomicReference<>(0.0);
    
    public TimeWindowStats(LocalDateTime start, LocalDateTime end) {
        this.windowStart = start;
        this.windowEnd = end;
        this.windowDurationSeconds = java.time.Duration.between(start, end).getSeconds();
    }
    
    /**
     * 记录新连接
     */
    public void recordNewConnection() {
        newConnections.incrementAndGet();
        totalConnections.incrementAndGet();
        updatePeakConnections();
    }
    
    /**
     * 记录连接关闭
     */
    public void recordConnectionClosed() {
        closedConnections.incrementAndGet();
        long current = activeConnections.decrementAndGet();
        if (current < 0) {
            activeConnections.set(0);
        }
    }
    
    /**
     * 更新活跃连接数
     */
    public void updateActiveConnections(long count) {
        activeConnections.set(count);
        updatePeakConnections();
    }
    
    /**
     * 更新峰值连接数
     */
    private void updatePeakConnections() {
        long current = activeConnections.get();
        peakConnections.updateAndGet(peak -> Math.max(peak, current));
    }
    
    /**
     * 记录数据读取
     */
    public void recordBytesRead(long bytes) {
        totalBytesRead.addAndGet(bytes);
    }
    
    /**
     * 记录数据写入
     */
    public void recordBytesWritten(long bytes) {
        totalBytesWritten.addAndGet(bytes);
    }
    
    /**
     * 记录消息读取
     */
    public void recordMessageRead() {
        totalMessagesRead.incrementAndGet();
    }
    
    /**
     * 记录消息写入
     */
    public void recordMessageWritten() {
        totalMessagesWritten.incrementAndGet();
    }
    
    /**
     * 记录请求
     */
    public void recordRequest(double responseTimeMs) {
        totalRequests.incrementAndGet();
        
        // 更新响应时间统计
        updateResponseTimeStats(responseTimeMs);
    }
    
    /**
     * 记录成功请求
     */
    public void recordSuccessfulRequest() {
        successfulRequests.incrementAndGet();
    }
    
    /**
     * 记录错误
     */
    public void recordError() {
        totalErrors.incrementAndGet();
        updateErrorRate();
    }
    
    /**
     * 更新响应时间统计
     */
    private void updateResponseTimeStats(double responseTimeMs) {
        // 更新最小响应时间
        minResponseTime.updateAndGet(min -> Math.min(min, responseTimeMs));
        
        // 更新最大响应时间
        maxResponseTime.updateAndGet(max -> Math.max(max, responseTimeMs));
        
        // 计算平均响应时间（简化版本，实际应该使用更精确的算法）
        long requests = totalRequests.get();
        if (requests > 0) {
            double currentAvg = avgResponseTime.get();
            double newAvg = (currentAvg * (requests - 1) + responseTimeMs) / requests;
            avgResponseTime.set(newAvg);
        }
    }
    
    /**
     * 更新错误率
     */
    private void updateErrorRate() {
        long requests = totalRequests.get();
        if (requests > 0) {
            double rate = (double) totalErrors.get() / requests * 100;
            errorRate.set(rate);
        }
    }
    
    /**
     * 记录缓冲区分配
     */
    public void recordBufferAllocation(long size) {
        totalBufferAllocations.incrementAndGet();
        peakBufferUsage.updateAndGet(peak -> Math.max(peak, size));
    }
    
    /**
     * 记录缓冲区释放
     */
    public void recordBufferDeallocation() {
        totalBufferDeallocations.incrementAndGet();
    }
    
    /**
     * 更新缓冲区利用率
     */
    public void updateBufferUtilization(double utilization) {
        // 简化版本的平均利用率计算
        double current = avgBufferUtilization.get();
        double newAvg = (current + utilization) / 2;
        avgBufferUtilization.set(newAvg);
    }
    
    /**
     * 计算吞吐量指标
     */
    public void calculateThroughputMetrics() {
        if (windowDurationSeconds > 0) {
            // 计算TPS (基于成功请求)
            double tpsValue = (double) successfulRequests.get() / windowDurationSeconds;
            tps.set(tpsValue);
            
            // 计算QPS (基于总请求)
            double qpsValue = (double) totalRequests.get() / windowDurationSeconds;
            qps.set(qpsValue);
            
            // 计算字节传输速率
            long totalBytes = totalBytesRead.get() + totalBytesWritten.get();
            double bytesPerSec = (double) totalBytes / windowDurationSeconds;
            bytesPerSecond.set(bytesPerSec);
        }
    }
    
    /**
     * 重置统计数据
     */
    public void reset() {
        totalConnections.set(0);
        activeConnections.set(0);
        newConnections.set(0);
        closedConnections.set(0);
        peakConnections.set(0);
        
        totalBytesRead.set(0);
        totalBytesWritten.set(0);
        totalMessagesRead.set(0);
        totalMessagesWritten.set(0);
        
        avgResponseTime.set(0.0);
        minResponseTime.set(Double.MAX_VALUE);
        maxResponseTime.set(0.0);
        totalRequests.set(0);
        successfulRequests.set(0);
        
        totalErrors.set(0);
        errorRate.set(0.0);
        
        tps.set(0.0);
        qps.set(0.0);
        bytesPerSecond.set(0.0);
        
        totalBufferAllocations.set(0);
        totalBufferDeallocations.set(0);
        peakBufferUsage.set(0);
        avgBufferUtilization.set(0.0);
    }
    
    /**
     * 合并另一个时间窗口的统计数据
     */
    public void merge(TimeWindowStats other) {
        totalConnections.addAndGet(other.totalConnections.get());
        newConnections.addAndGet(other.newConnections.get());
        closedConnections.addAndGet(other.closedConnections.get());
        peakConnections.updateAndGet(peak -> Math.max(peak, other.peakConnections.get()));
        
        totalBytesRead.addAndGet(other.totalBytesRead.get());
        totalBytesWritten.addAndGet(other.totalBytesWritten.get());
        totalMessagesRead.addAndGet(other.totalMessagesRead.get());
        totalMessagesWritten.addAndGet(other.totalMessagesWritten.get());
        
        totalRequests.addAndGet(other.totalRequests.get());
        successfulRequests.addAndGet(other.successfulRequests.get());
        totalErrors.addAndGet(other.totalErrors.get());
        
        totalBufferAllocations.addAndGet(other.totalBufferAllocations.get());
        totalBufferDeallocations.addAndGet(other.totalBufferDeallocations.get());
        peakBufferUsage.updateAndGet(peak -> Math.max(peak, other.peakBufferUsage.get()));
        
        // 重新计算派生指标
        updateErrorRate();
        calculateThroughputMetrics();
    }
    
    /**
     * 获取统计摘要
     */
    public StatsSummary getSummary() {
        calculateThroughputMetrics();
        
        return StatsSummary.builder()
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .totalConnections(totalConnections.get())
                .activeConnections(activeConnections.get())
                .peakConnections(peakConnections.get())
                .totalBytes(totalBytesRead.get() + totalBytesWritten.get())
                .totalRequests(totalRequests.get())
                .successfulRequests(successfulRequests.get())
                .errorRate(errorRate.get())
                .avgResponseTime(avgResponseTime.get())
                .tps(tps.get())
                .qps(qps.get())
                .bytesPerSecond(bytesPerSecond.get())
                .build();
    }
    
    @Data
    @lombok.Builder
    public static class StatsSummary {
        private LocalDateTime windowStart;
        private LocalDateTime windowEnd;
        private long totalConnections;
        private long activeConnections;
        private long peakConnections;
        private long totalBytes;
        private long totalRequests;
        private long successfulRequests;
        private double errorRate;
        private double avgResponseTime;
        private double tps;
        private double qps;
        private double bytesPerSecond;
    }
}