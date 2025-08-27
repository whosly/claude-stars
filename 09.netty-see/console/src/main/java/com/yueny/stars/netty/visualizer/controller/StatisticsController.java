package com.yueny.stars.netty.visualizer.controller;

import com.yueny.stars.netty.visualizer.model.statistics.ApplicationStats;
import com.yueny.stars.netty.visualizer.model.statistics.EventLoopStats;
import com.yueny.stars.netty.visualizer.model.statistics.TimeWindowStats;
import com.yueny.stars.netty.visualizer.service.StatisticsAggregationService;
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
    
    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeStats() {
        try {
            TimeWindowStats.StatsSummary stats = statisticsService.getRealTimeStats();
            
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
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting real-time statistics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get real-time statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
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
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusMinutes(minutes);
            
            List<TimeWindowStats.StatsSummary> stats = statisticsService.getTimeRangeStats(start, end, "minute");
            
            Map<String, Object> result = new HashMap<>();
            result.put("minutes", minutes);
            result.put("start", start);
            result.put("end", end);
            result.put("dataPoints", stats.size());
            result.put("data", stats);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting recent statistics: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get recent statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
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