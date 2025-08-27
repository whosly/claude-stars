package com.yueny.stars.netty.visualizer.model.statistics;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;

/**
 * 应用维度统计信息
 * 
 * @author fengyang
 */
@Data
public class ApplicationStats {
    
    private String applicationName;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    
    // 连接统计
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong currentConnections = new AtomicLong(0);
    private final AtomicLong peakConnections = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);
    
    // 数据传输统计
    private final AtomicLong totalBytesRead = new AtomicLong(0);
    private final AtomicLong totalBytesWritten = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    
    // 性能统计
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicReference<Double> avgResponseTime = new AtomicReference<>(0.0);
    private final AtomicReference<Double> minResponseTime = new AtomicReference<>(Double.MAX_VALUE);
    private final AtomicReference<Double> maxResponseTime = new AtomicReference<>(0.0);
    
    // 错误统计
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();
    private final AtomicReference<Double> errorRate = new AtomicReference<>(0.0);
    
    // 资源使用统计
    private final AtomicLong totalBufferAllocations = new AtomicLong(0);
    private final AtomicLong currentBufferUsage = new AtomicLong(0);
    private final AtomicLong peakBufferUsage = new AtomicLong(0);
    private final AtomicReference<Double> avgBufferUtilization = new AtomicReference<>(0.0);
    
    // EventLoop统计
    private final Map<String, AtomicLong> eventLoopUsage = new ConcurrentHashMap<>();
    
    // 地理位置统计 (基于IP地址)
    private final Map<String, AtomicLong> connectionsByRegion = new ConcurrentHashMap<>();
    
    // 时间分布统计
    private final Map<Integer, AtomicLong> connectionsByHour = new ConcurrentHashMap<>();
    
    public ApplicationStats(String applicationName) {
        this.applicationName = applicationName;
        this.firstSeen = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
        
        // 初始化24小时统计
        for (int i = 0; i < 24; i++) {
            connectionsByHour.put(i, new AtomicLong(0));
        }
    }
    
    /**
     * 记录新连接
     */
    public void recordNewConnection(String remoteAddress) {
        totalConnections.incrementAndGet();
        long current = currentConnections.incrementAndGet();
        peakConnections.updateAndGet(peak -> Math.max(peak, current));
        
        // 记录时间分布
        int hour = LocalDateTime.now().getHour();
        connectionsByHour.get(hour).incrementAndGet();
        
        // 记录地理位置 (简化版本，基于IP前缀)
        String region = extractRegion(remoteAddress);
        connectionsByRegion.computeIfAbsent(region, k -> new AtomicLong(0)).incrementAndGet();
        
        updateLastSeen();
    }
    
    /**
     * 记录连接关闭
     */
    public void recordConnectionClosed() {
        long current = currentConnections.decrementAndGet();
        if (current < 0) {
            currentConnections.set(0);
        }
        updateLastSeen();
    }
    
    /**
     * 记录连接失败
     */
    public void recordConnectionFailure() {
        connectionFailures.incrementAndGet();
        updateLastSeen();
    }
    
    /**
     * 记录数据传输
     */
    public void recordDataTransfer(long bytesRead, long bytesWritten, int messages) {
        totalBytesRead.addAndGet(bytesRead);
        totalBytesWritten.addAndGet(bytesWritten);
        totalMessages.addAndGet(messages);
        updateLastSeen();
    }
    
    /**
     * 记录请求
     */
    public void recordRequest(double responseTimeMs, boolean success) {
        totalRequests.incrementAndGet();
        
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        
        // 更新响应时间统计
        updateResponseTimeStats(responseTimeMs);
        updateErrorRate();
        updateLastSeen();
    }
    
    /**
     * 记录错误
     */
    public void recordError(String errorType) {
        totalErrors.incrementAndGet();
        errorsByType.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
        updateErrorRate();
        updateLastSeen();
    }
    
    /**
     * 记录缓冲区使用
     */
    public void recordBufferUsage(long allocated, long current, double utilization) {
        if (allocated > 0) {
            totalBufferAllocations.incrementAndGet();
        }
        
        currentBufferUsage.set(current);
        peakBufferUsage.updateAndGet(peak -> Math.max(peak, current));
        
        // 更新平均利用率
        double currentAvg = avgBufferUtilization.get();
        double newAvg = (currentAvg + utilization) / 2;
        avgBufferUtilization.set(newAvg);
        
        updateLastSeen();
    }
    
    /**
     * 记录EventLoop使用
     */
    public void recordEventLoopUsage(String eventLoopType) {
        eventLoopUsage.computeIfAbsent(eventLoopType, k -> new AtomicLong(0)).incrementAndGet();
        updateLastSeen();
    }
    
    /**
     * 更新响应时间统计
     */
    private void updateResponseTimeStats(double responseTimeMs) {
        minResponseTime.updateAndGet(min -> Math.min(min, responseTimeMs));
        maxResponseTime.updateAndGet(max -> Math.max(max, responseTimeMs));
        
        // 计算平均响应时间
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
            double rate = (double) failedRequests.get() / requests * 100;
            errorRate.set(rate);
        }
    }
    
    /**
     * 提取地理区域信息 (简化版本)
     */
    private String extractRegion(String remoteAddress) {
        if (remoteAddress == null || remoteAddress.isEmpty()) {
            return "Unknown";
        }
        
        // 简化的地理位置判断
        if (remoteAddress.startsWith("127.") || remoteAddress.startsWith("localhost")) {
            return "Local";
        } else if (remoteAddress.startsWith("192.168.") || remoteAddress.startsWith("10.") || 
                   remoteAddress.startsWith("172.")) {
            return "Internal";
        } else {
            // 基于IP前缀的简单区域划分
            String[] parts = remoteAddress.split("\\.");
            if (parts.length >= 2) {
                return "Region-" + parts[0] + "." + parts[1];
            }
            return "External";
        }
    }
    
    /**
     * 更新最后活跃时间
     */
    private void updateLastSeen() {
        lastSeen = LocalDateTime.now();
    }
    
    /**
     * 获取连接成功率
     */
    public double getConnectionSuccessRate() {
        long total = totalConnections.get();
        long failures = connectionFailures.get();
        if (total > 0) {
            return (double) (total - failures) / total * 100;
        }
        return 100.0;
    }
    
    /**
     * 获取平均吞吐量 (字节/秒)
     */
    public double getAvgThroughput() {
        long duration = java.time.Duration.between(firstSeen, lastSeen).getSeconds();
        if (duration > 0) {
            long totalBytes = totalBytesRead.get() + totalBytesWritten.get();
            return (double) totalBytes / duration;
        }
        return 0.0;
    }
    
    /**
     * 获取平均TPS
     */
    public double getAvgTps() {
        long duration = java.time.Duration.between(firstSeen, lastSeen).getSeconds();
        if (duration > 0) {
            return (double) successfulRequests.get() / duration;
        }
        return 0.0;
    }
    
    /**
     * 获取最活跃的错误类型
     */
    public String getMostCommonErrorType() {
        return errorsByType.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("None");
    }
    
    /**
     * 获取最活跃的EventLoop类型
     */
    public String getMostUsedEventLoopType() {
        return eventLoopUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * 获取最活跃的连接区域
     */
    public String getMostActiveRegion() {
        return connectionsByRegion.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * 获取峰值连接时间
     */
    public int getPeakConnectionHour() {
        return connectionsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse(0);
    }
    
    /**
     * 重置统计数据
     */
    public void reset() {
        totalConnections.set(0);
        currentConnections.set(0);
        peakConnections.set(0);
        connectionFailures.set(0);
        
        totalBytesRead.set(0);
        totalBytesWritten.set(0);
        totalMessages.set(0);
        
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        avgResponseTime.set(0.0);
        minResponseTime.set(Double.MAX_VALUE);
        maxResponseTime.set(0.0);
        
        totalErrors.set(0);
        errorsByType.clear();
        errorRate.set(0.0);
        
        totalBufferAllocations.set(0);
        currentBufferUsage.set(0);
        peakBufferUsage.set(0);
        avgBufferUtilization.set(0.0);
        
        eventLoopUsage.clear();
        connectionsByRegion.clear();
        connectionsByHour.values().forEach(counter -> counter.set(0));
        
        firstSeen = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
    }
    
    /**
     * 获取统计摘要
     */
    public ApplicationStatsSummary getSummary() {
        return ApplicationStatsSummary.builder()
                .applicationName(applicationName)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .totalConnections(totalConnections.get())
                .currentConnections(currentConnections.get())
                .peakConnections(peakConnections.get())
                .connectionSuccessRate(getConnectionSuccessRate())
                .totalBytes(totalBytesRead.get() + totalBytesWritten.get())
                .totalRequests(totalRequests.get())
                .successRate((double) successfulRequests.get() / Math.max(1, totalRequests.get()) * 100)
                .avgResponseTime(avgResponseTime.get())
                .errorRate(errorRate.get())
                .avgThroughput(getAvgThroughput())
                .avgTps(getAvgTps())
                .mostCommonErrorType(getMostCommonErrorType())
                .mostUsedEventLoopType(getMostUsedEventLoopType())
                .mostActiveRegion(getMostActiveRegion())
                .peakConnectionHour(getPeakConnectionHour())
                .build();
    }
    
    @Data
    @lombok.Builder
    public static class ApplicationStatsSummary {
        private String applicationName;
        private LocalDateTime firstSeen;
        private LocalDateTime lastSeen;
        private long totalConnections;
        private long currentConnections;
        private long peakConnections;
        private double connectionSuccessRate;
        private long totalBytes;
        private long totalRequests;
        private double successRate;
        private double avgResponseTime;
        private double errorRate;
        private double avgThroughput;
        private double avgTps;
        private String mostCommonErrorType;
        private String mostUsedEventLoopType;
        private String mostActiveRegion;
        private int peakConnectionHour;
    }
}