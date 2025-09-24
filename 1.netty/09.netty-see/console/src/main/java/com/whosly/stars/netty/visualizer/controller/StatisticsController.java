package com.whosly.stars.netty.visualizer.controller;

import com.whosly.stars.netty.visualizer.model.ChannelInfo;
import com.whosly.stars.netty.visualizer.model.statistics.ApplicationStats;
import com.whosly.stars.netty.visualizer.model.statistics.EventLoopStats;
import com.whosly.stars.netty.visualizer.model.statistics.TimeWindowStats;
import com.whosly.stars.netty.visualizer.service.NettyMonitorService;
import com.whosly.stars.netty.visualizer.service.StatisticsAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析控制器
 * 提供各种维度的统计数据查询接口
 * 
 * @author fengyang
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsAggregationService statisticsService;
    
    // 注入NettyMonitorService来获取实时数据
    @Autowired
    private NettyMonitorService nettyMonitorService;
    
    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeStats() {
        try {
            // 检查统计服务可用性
            if (statisticsService == null) {
                log.warn("StatisticsAggregationService is not available, using NettyMonitorService data");
                return getRealTimeStatsFromMonitor();
            }
            
            // 优先使用StatisticsAggregationService获取真实数据
            TimeWindowStats.StatsSummary stats = statisticsService.getRealTimeStats();
            
            // 如果统计服务没有数据，从NettyMonitorService获取当前状态
            if (stats.getTotalConnections() == 0 && stats.getActiveConnections() == 0) {
                log.info("StatisticsAggregationService has no data, using current channel state");
                return getRealTimeStatsFromMonitor();
            }
            
            // 验证数据一致性
            if (!isValidStatsData(stats)) {
                log.warn("Statistics data consistency check failed: {}, falling back to monitor data", stats);
                return getRealTimeStatsFromMonitor();
            }
            
            // 返回真实统计数据
            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("activeConnections", stats.getActiveConnections());
            result.put("totalConnections", stats.getTotalConnections());
            result.put("totalBytes", stats.getTotalBytes());
            result.put("totalRequests", stats.getTotalRequests());
            result.put("successfulRequests", stats.getSuccessfulRequests());
            result.put("tps", stats.getTps());
            result.put("qps", stats.getQps());
            result.put("avgResponseTime", stats.getAvgResponseTime());
            result.put("errorRate", stats.getErrorRate());
            result.put("bytesPerSecond", stats.getBytesPerSecond());
            result.put("dataSource", "aggregated");
            result.put("isRealData", true);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error getting real-time statistics: {}", e.getMessage(), e);
            return getRealTimeStatsFromMonitor();
        }
    }
    
    /**
     * 从NettyMonitorService获取实时统计数据
     */
    private ResponseEntity<Map<String, Object>> getRealTimeStatsFromMonitor() {
        try {
            // 从NettyMonitorService获取当前Channel数据
            List<ChannelInfo> channels = nettyMonitorService.getAllChannels();
            
            // 计算基础连接统计
            long activeConnections = channels.stream().mapToLong(ch -> ch.isActive() ? 1 : 0).sum();
            long totalConnections = channels.size();
            long totalBytesRead = channels.stream().mapToLong(ChannelInfo::getBytesRead).sum();
            long totalBytesWritten = channels.stream().mapToLong(ChannelInfo::getBytesWritten).sum();
            long totalBytes = totalBytesRead + totalBytesWritten;
            
            // 计算请求统计 - 基于实际的消息读取数（通常代表请求）
            long totalRequests = channels.stream().mapToLong(ChannelInfo::getMessagesRead).sum();
            
            // 计算错误统计 - 检查有错误信息的Channel
            long errorChannels = channels.stream()
                .mapToLong(ch -> (ch.getErrorMessage() != null && !ch.getErrorMessage().isEmpty()) ? 1 : 0)
                .sum();
            
            // 基于错误Channel计算错误率和成功请求数
            double errorRate = totalConnections > 0 ? (double) errorChannels / totalConnections * 100 : 0.0;
            long successfulRequests = Math.max(0, totalRequests - errorChannels);
            
            // 计算平均响应时间 - 基于连接建立到最后活跃的时间差
            double avgResponseTime = channels.stream()
                .filter(ch -> ch.isActive() && ch.getLastActiveTime() != null && ch.getCreateTime() != null)
                .mapToDouble(ch -> {
                    long duration = java.time.Duration.between(ch.getCreateTime(), ch.getLastActiveTime()).toMillis();
                    // 合理的响应时间范围：1ms到5000ms
                    return Math.max(1, Math.min(duration, 5000));
                })
                .average()
                .orElse(0.0);
            
            // 计算吞吐量指标 - 基于连接的生命周期
            LocalDateTime now = LocalDateTime.now();
            double totalDurationSeconds = channels.stream()
                .filter(ch -> ch.getCreateTime() != null)
                .mapToDouble(ch -> {
                    LocalDateTime endTime = ch.getLastActiveTime() != null ? ch.getLastActiveTime() : now;
                    long durationMillis = java.time.Duration.between(ch.getCreateTime(), endTime).toMillis();
                    return durationMillis / 1000.0; // 转换为秒
                })
                .sum();
            
            // 避免除零错误
            totalDurationSeconds = Math.max(totalDurationSeconds, 1.0);
            
            double tps = totalRequests / totalDurationSeconds;
            double qps = totalRequests / totalDurationSeconds; // 在这个场景下TPS和QPS相同
            double bytesPerSecond = totalBytes / totalDurationSeconds;
            
            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("activeConnections", activeConnections);
            result.put("totalConnections", totalConnections);
            result.put("totalBytes", totalBytes);
            result.put("totalRequests", totalRequests);
            result.put("successfulRequests", successfulRequests);
            result.put("tps", Math.round(tps * 100.0) / 100.0); // 保留2位小数
            result.put("qps", Math.round(qps * 100.0) / 100.0);
            result.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
            result.put("errorRate", Math.round(errorRate * 100.0) / 100.0);
            result.put("bytesPerSecond", Math.round(bytesPerSecond * 100.0) / 100.0);
            result.put("dataSource", "monitor");
            result.put("isRealData", true);
            
            log.info("Real-time stats: {} active connections, {} requests, {:.2f}% error rate, {:.2f}ms avg response time", 
                    activeConnections, totalRequests, errorRate, avgResponseTime);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error getting real-time statistics from monitor: {}", e.getMessage(), e);
            return createErrorResponse("Failed to retrieve statistics from monitor: " + e.getMessage());
        }
    }
    
    /**
     * 验证统计数据的一致性
     */
    private boolean isValidStatsData(TimeWindowStats.StatsSummary stats) {
        if (stats == null) {
            return false;
        }
        
        // 检查基本数据有效性
        if (stats.getTotalRequests() < 0 || 
            stats.getSuccessfulRequests() < 0 || 
            stats.getErrorRate() < 0 || 
            stats.getErrorRate() > 100) {
            return false;
        }
        
        // 检查数据逻辑一致性
        long errorRequests = stats.getTotalRequests() - stats.getSuccessfulRequests();
        if (errorRequests < 0) {
            return false;
        }
        
        // 检查错误率计算是否正确
        if (stats.getTotalRequests() > 0) {
            double expectedErrorRate = (double) errorRequests / stats.getTotalRequests() * 100;
            if (Math.abs(stats.getErrorRate() - expectedErrorRate) > 0.1) {
                return false;
            }
        }
        
        return true;
    }
    

    
    /**
     * 创建错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("error", message);
        error.put("dataSource", "error");
        error.put("isRealData", false);
        return ResponseEntity.internalServerError().body(error);
    }
    
    /**
     * 获取指定时间范围的统计数据
     */
    @GetMapping("/timerange")
    public ResponseEntity<Map<String, Object>> getTimeRangeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "minute") String granularity) {
        
        try {
            List<TimeWindowStats.StatsSummary> stats = statisticsService.getTimeRangeStats(start, end, granularity);
            
            Map<String, Object> result = new HashMap<>();
            result.put("start", start);
            result.put("end", end);
            result.put("granularity", granularity);
            result.put("dataPoints", stats.size());
            result.put("data", stats);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting time range statistics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get time range statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取最近N分钟的统计数据
     */
    @GetMapping("/recent/{minutes}")
    public ResponseEntity<Map<String, Object>> getRecentStats(@PathVariable int minutes) {
        try {
            // 检查统计服务可用性
            if (statisticsService == null) {
                log.warn("StatisticsAggregationService is not available");
                return createEmptyTrendResponse(minutes, "Statistics service not available");
            }
            
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusMinutes(minutes);
            
            // 从StatisticsAggregationService获取真实数据
            List<TimeWindowStats.StatsSummary> stats = statisticsService.getTimeRangeStats(start, end, "minute");
            
            Map<String, Object> result = new HashMap<>();
            result.put("minutes", minutes);
            result.put("start", start);
            result.put("end", end);
            result.put("dataPoints", stats != null ? stats.size() : 0);
            result.put("data", stats != null ? stats : new ArrayList<>());
            result.put("dataSource", stats != null && !stats.isEmpty() ? "real" : "empty");
            result.put("isRealData", stats != null && !stats.isEmpty());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error getting recent statistics: {}", e.getMessage(), e);
            return createEmptyTrendResponse(minutes, "Failed to get recent statistics: " + e.getMessage());
        }
    }
    
    /**
     * 创建空的趋势数据响应
     */
    private ResponseEntity<Map<String, Object>> createEmptyTrendResponse(int minutes, String message) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(minutes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("minutes", minutes);
        result.put("start", start);
        result.put("end", end);
        result.put("dataPoints", 0);
        result.put("data", new ArrayList<>());
        result.put("dataSource", "unavailable");
        result.put("isRealData", false);
        result.put("message", message);
        
        return ResponseEntity.ok(result);
    }
    

    
    /**
     * 获取所有应用的统计数据
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getAllApplicationStats() {
        try {
            List<ApplicationStats.ApplicationStatsSummary> appStats = statisticsService.getAllApplicationStats();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalApplications", appStats.size());
            result.put("timestamp", LocalDateTime.now());
            result.put("applications", appStats);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting application statistics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get application statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取指定应用的统计数据
     */
    @GetMapping("/applications/{applicationName}")
    public ResponseEntity<Map<String, Object>> getApplicationStats(@PathVariable String applicationName) {
        try {
            ApplicationStats.ApplicationStatsSummary stats = statisticsService.getApplicationStats(applicationName);
            
            if (stats == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Application not found: " + applicationName);
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("applicationName", applicationName);
            result.put("timestamp", LocalDateTime.now());
            result.put("statistics", stats);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting application statistics for {}: {}", applicationName, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get application statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取性能指标
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            TimeWindowStats.StatsSummary realTimeStats = statisticsService.getRealTimeStats();
            
            Map<String, Object> performance = new HashMap<>();
            performance.put("tps", realTimeStats.getTps());
            performance.put("qps", realTimeStats.getQps());
            performance.put("avgResponseTime", realTimeStats.getAvgResponseTime());
            performance.put("errorRate", realTimeStats.getErrorRate());
            performance.put("bytesPerSecond", realTimeStats.getBytesPerSecond());
            performance.put("activeConnections", realTimeStats.getActiveConnections());
            performance.put("peakConnections", realTimeStats.getPeakConnections());
            
            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("performance", performance);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting performance metrics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get performance metrics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取错误统计
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorStats() {
        try {
            List<ApplicationStats.ApplicationStatsSummary> appStats = statisticsService.getAllApplicationStats();
            
            // 聚合错误统计
            Map<String, Long> errorsByType = new HashMap<>();
            long totalErrors = 0;
            
            for (ApplicationStats.ApplicationStatsSummary app : appStats) {
                // 基于错误率和总请求数计算错误数
                long appErrors = (long) (app.getTotalRequests() * app.getErrorRate() / 100.0);
                totalErrors += appErrors;
                
                // 收集错误类型
                if (app.getMostCommonErrorType() != null && appErrors > 0) {
                    errorsByType.put(app.getMostCommonErrorType(), 
                            errorsByType.getOrDefault(app.getMostCommonErrorType(), 0L) + appErrors);
                }
            }
            
            List<Map<String, Object>> topErrorApps = appStats.stream()
                    .filter(app -> app.getErrorRate() > 0)
                    .sorted((a, b) -> Double.compare(b.getErrorRate(), a.getErrorRate()))
                    .limit(10)
                    .map(app -> {
                        Map<String, Object> appError = new HashMap<>();
                        appError.put("applicationName", app.getApplicationName());
                        appError.put("totalRequests", app.getTotalRequests());
                        appError.put("errorRate", app.getErrorRate());
                        return appError;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("totalErrors", totalErrors);
            result.put("errorsByType", errorsByType);
            result.put("topErrorApplications", topErrorApps);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting error statistics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get error statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取所有EventLoop统计
     */
    @GetMapping("/eventloops")
    public ResponseEntity<Map<String, Object>> getAllEventLoopStats() {
        try {
            List<EventLoopStats.EventLoopStatsSummary> elStats = statisticsService.getAllEventLoopStats();
            
            List<Map<String, Object>> eventLoops = elStats.stream()
                    .map(el -> {
                        Map<String, Object> elMap = new HashMap<>();
                        elMap.put("eventLoopName", el.getEventLoopName());
                        elMap.put("currentChannels", el.getCurrentChannels());
                        elMap.put("totalChannels", el.getTotalChannels());
                        elMap.put("isOverloaded", el.isOverloaded());
                        return elMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("totalEventLoops", elStats.size());
            result.put("eventLoops", eventLoops);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting EventLoop statistics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get EventLoop statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取指定EventLoop统计
     */
    @GetMapping("/eventloops/{eventLoopName}")
    public ResponseEntity<Map<String, Object>> getEventLoopStats(@PathVariable String eventLoopName) {
        try {
            EventLoopStats.EventLoopStatsSummary stats = statisticsService.getEventLoopStats(eventLoopName);
            
            if (stats == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> eventLoop = new HashMap<>();
            eventLoop.put("eventLoopName", stats.getEventLoopName());
            eventLoop.put("eventLoopType", stats.getEventLoopType());
            eventLoop.put("currentChannels", stats.getCurrentChannels());
            eventLoop.put("totalChannels", stats.getTotalChannels());
            eventLoop.put("peakChannels", stats.getPeakChannels());
            eventLoop.put("avgThroughput", stats.getAvgThroughput());
            eventLoop.put("isOverloaded", stats.isOverloaded());
            eventLoop.put("errorRate", stats.getErrorRate());
            
            Map<String, Object> result = new HashMap<>();
            result.put("timestamp", LocalDateTime.now());
            result.put("eventLoop", eventLoop);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting EventLoop statistics for {}: {}", eventLoopName, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get EventLoop statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取统计概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getStatisticsOverview() {
        try {
            Map<String, Object> overview = statisticsService.getStatisticsOverview();
            overview.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("Error getting statistics overview: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get statistics overview: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 重置统计数据
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetStatistics() {
        try {
            statisticsService.resetAllStats();
            Map<String, String> result = new HashMap<>();
            result.put("message", "Statistics data has been reset successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error resetting statistics: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}