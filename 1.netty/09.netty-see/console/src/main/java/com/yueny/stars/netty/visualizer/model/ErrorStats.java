package com.yueny.stars.netty.visualizer.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 错误统计信息模型
 * 
 * @author fengyang
 */
@Data
public class ErrorStats {
    
    // 总错误数
    private long totalErrors = 0;
    
    // 总请求数
    private long totalRequests = 0;
    
    // 错误率 (%)
    private double errorRate = 0.0;
    
    // 按错误类型分类的统计
    private Map<String, Long> errorsByType = new ConcurrentHashMap<>();
    
    // 按应用分类的错误统计
    private Map<String, Long> errorsByApplication = new ConcurrentHashMap<>();
    
    // 按Channel分类的错误统计
    private Map<String, Long> errorsByChannel = new ConcurrentHashMap<>();
    
    // 最近的错误记录
    private Map<String, ErrorRecord> recentErrors = new ConcurrentHashMap<>();
    
    // 统计时间窗口
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    
    /**
     * 错误记录详情
     */
    @Data
    public static class ErrorRecord {
        private String channelId;
        private String applicationName;
        private String errorType;
        private String errorMessage;
        private String stackTrace;
        private LocalDateTime timestamp;
        private String remoteAddress;
        private String localAddress;
        private long count = 1; // 相同错误的重复次数
    }
    
    /**
     * 计算错误率
     */
    public void calculateErrorRate() {
        if (totalRequests > 0) {
            this.errorRate = (double) totalErrors / totalRequests * 100.0;
        } else {
            this.errorRate = 0.0;
        }
    }
    
    /**
     * 增加错误计数
     */
    public void incrementError(String errorType, String applicationName, String channelId) {
        totalErrors++;
        errorsByType.merge(errorType, 1L, Long::sum);
        if (applicationName != null) {
            errorsByApplication.merge(applicationName, 1L, Long::sum);
        }
        if (channelId != null) {
            errorsByChannel.merge(channelId, 1L, Long::sum);
        }
        calculateErrorRate();
    }
    
    /**
     * 增加请求计数
     */
    public void incrementRequest() {
        totalRequests++;
        calculateErrorRate();
    }
    
    /**
     * 重置统计
     */
    public void reset() {
        totalErrors = 0;
        totalRequests = 0;
        errorRate = 0.0;
        errorsByType.clear();
        errorsByApplication.clear();
        errorsByChannel.clear();
        recentErrors.clear();
        windowStart = LocalDateTime.now();
        windowEnd = null;
    }
}