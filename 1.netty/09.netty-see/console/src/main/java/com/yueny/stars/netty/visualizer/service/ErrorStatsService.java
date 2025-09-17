package com.yueny.stars.netty.visualizer.service;

import com.yueny.stars.netty.visualizer.model.ErrorStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 错误统计服务
 * 
 * @author fengyang
 */
@Slf4j
@Service
public class ErrorStatsService {
    
    // 当前统计窗口
    private final ErrorStats currentStats = new ErrorStats();
    
    // 历史统计数据（按小时存储）
    private final Map<String, ErrorStats> hourlyStats = new ConcurrentHashMap<>();
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // 最大保留历史数据小时数
    private static final int MAX_HISTORY_HOURS = 24;
    
    @PostConstruct
    public void init() {
        currentStats.setWindowStart(LocalDateTime.now());
        
        // 每分钟更新一次统计
        scheduler.scheduleAtFixedRate(this::updateStats, 1, 1, TimeUnit.MINUTES);
        
        // 每小时归档一次数据
        scheduler.scheduleAtFixedRate(this::archiveHourlyStats, 1, 1, TimeUnit.HOURS);
        
        log.info("Error statistics service initialized");
    }
    
    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        log.info("Error statistics service stopped");
    }
    
    /**
     * 记录异常
     */
    public void recordException(String channelId, String applicationName, Throwable throwable, 
                               String remoteAddress, String localAddress) {
        String errorType = throwable.getClass().getSimpleName();
        String errorMessage = throwable.getMessage();
        String stackTrace = getStackTrace(throwable);
        
        // 更新统计计数
        currentStats.incrementError(errorType, applicationName, channelId);
        
        // 创建错误记录
        ErrorStats.ErrorRecord record = new ErrorStats.ErrorRecord();
        record.setChannelId(channelId);
        record.setApplicationName(applicationName);
        record.setErrorType(errorType);
        record.setErrorMessage(errorMessage);
        record.setStackTrace(stackTrace);
        record.setTimestamp(LocalDateTime.now());
        record.setRemoteAddress(remoteAddress);
        record.setLocalAddress(localAddress);
        
        // 生成错误记录的唯一键
        String errorKey = generateErrorKey(channelId, errorType, errorMessage);
        
        // 检查是否是重复错误
        ErrorStats.ErrorRecord existingRecord = currentStats.getRecentErrors().get(errorKey);
        if (existingRecord != null) {
            // 更新重复次数和时间戳
            existingRecord.setCount(existingRecord.getCount() + 1);
            existingRecord.setTimestamp(LocalDateTime.now());
        } else {
            // 新错误记录
            currentStats.getRecentErrors().put(errorKey, record);
            
            // 限制最近错误记录数量
            if (currentStats.getRecentErrors().size() > 100) {
                cleanupOldErrors();
            }
        }
        
        log.warn("Recorded error for channel {}: {} - {}", channelId, errorType, errorMessage);
    }
    
    /**
     * 记录成功请求
     */
    public void recordSuccess(String channelId, String applicationName) {
        currentStats.incrementRequest();
    }
    
    /**
     * 记录请求（无论成功失败）
     */
    public void recordRequest(String channelId, String applicationName) {
        currentStats.incrementRequest();
    }
    
    /**
     * 获取当前错误统计
     */
    public ErrorStats getCurrentStats() {
        currentStats.setWindowEnd(LocalDateTime.now());
        return currentStats;
    }
    
    /**
     * 获取指定时间范围的错误统计
     */
    public ErrorStats getStatsForPeriod(LocalDateTime start, LocalDateTime end) {
        ErrorStats periodStats = new ErrorStats();
        periodStats.setWindowStart(start);
        periodStats.setWindowEnd(end);
        
        // 合并指定时间范围内的统计数据
        for (Map.Entry<String, ErrorStats> entry : hourlyStats.entrySet()) {
            ErrorStats hourlyData = entry.getValue();
            if (isInPeriod(hourlyData, start, end)) {
                mergeStats(periodStats, hourlyData);
            }
        }
        
        // 如果当前统计在时间范围内，也要合并
        if (isInPeriod(currentStats, start, end)) {
            mergeStats(periodStats, currentStats);
        }
        
        periodStats.calculateErrorRate();
        return periodStats;
    }
    
    /**
     * 获取错误趋势数据（按小时）
     */
    public List<Map<String, Object>> getErrorTrend(int hours) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = hours - 1; i >= 0; i--) {
            LocalDateTime hourStart = now.minusHours(i).truncatedTo(ChronoUnit.HOURS);
            String hourKey = hourStart.toString();
            
            ErrorStats stats = hourlyStats.get(hourKey);
            if (stats == null) {
                stats = new ErrorStats();
            }
            
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("hour", hourStart.toString());
            dataPoint.put("totalErrors", stats.getTotalErrors());
            dataPoint.put("totalRequests", stats.getTotalRequests());
            dataPoint.put("errorRate", stats.getErrorRate());
            dataPoint.put("errorsByType", stats.getErrorsByType());
            
            trend.add(dataPoint);
        }
        
        return trend;
    }
    
    /**
     * 获取Top错误类型
     */
    public List<Map<String, Object>> getTopErrorTypes(int limit) {
        return currentStats.getErrorsByType().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("errorType", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
    
    /**
     * 获取Top错误应用
     */
    public List<Map<String, Object>> getTopErrorApplications(int limit) {
        return currentStats.getErrorsByApplication().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("applicationName", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
    
    /**
     * 获取最近的错误记录
     */
    public List<ErrorStats.ErrorRecord> getRecentErrors(int limit) {
        return currentStats.getRecentErrors().values().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
    
    /**
     * 更新统计信息
     */
    private void updateStats() {
        currentStats.calculateErrorRate();
        log.debug("Updated error stats - Total errors: {}, Total requests: {}, Error rate: {}%", 
                currentStats.getTotalErrors(), currentStats.getTotalRequests(), 
                String.format("%.2f", currentStats.getErrorRate()));
    }
    
    /**
     * 归档小时统计数据
     */
    private void archiveHourlyStats() {
        LocalDateTime now = LocalDateTime.now();
        String hourKey = now.truncatedTo(ChronoUnit.HOURS).toString();
        
        // 复制当前统计到小时归档
        ErrorStats hourlyData = new ErrorStats();
        hourlyData.setTotalErrors(currentStats.getTotalErrors());
        hourlyData.setTotalRequests(currentStats.getTotalRequests());
        hourlyData.setErrorRate(currentStats.getErrorRate());
        hourlyData.getErrorsByType().putAll(currentStats.getErrorsByType());
        hourlyData.getErrorsByApplication().putAll(currentStats.getErrorsByApplication());
        hourlyData.getErrorsByChannel().putAll(currentStats.getErrorsByChannel());
        hourlyData.setWindowStart(currentStats.getWindowStart());
        hourlyData.setWindowEnd(now);
        
        hourlyStats.put(hourKey, hourlyData);
        
        // 清理过期的历史数据
        cleanupOldHourlyStats();
        
        // 重置当前统计
        currentStats.reset();
        
        log.info("Archived hourly error stats for {}", hourKey);
    }
    
    /**
     * 清理过期的小时统计数据
     */
    private void cleanupOldHourlyStats() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(MAX_HISTORY_HOURS);
        
        hourlyStats.entrySet().removeIf(entry -> {
            LocalDateTime hourTime = LocalDateTime.parse(entry.getKey());
            return hourTime.isBefore(cutoff);
        });
    }
    
    /**
     * 清理过期的错误记录
     */
    private void cleanupOldErrors() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        
        currentStats.getRecentErrors().entrySet().removeIf(entry -> 
                entry.getValue().getTimestamp().isBefore(cutoff));
    }
    
    /**
     * 生成错误记录的唯一键
     */
    private String generateErrorKey(String channelId, String errorType, String errorMessage) {
        return String.format("%s:%s:%s", 
                channelId != null ? channelId : "unknown",
                errorType != null ? errorType : "unknown",
                errorMessage != null ? errorMessage.substring(0, Math.min(50, errorMessage.length())) : "unknown");
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
        
        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = 0; i < Math.min(5, elements.length); i++) {
            sb.append("\tat ").append(elements[i].toString()).append("\n");
        }
        
        if (elements.length > 5) {
            sb.append("\t... ").append(elements.length - 5).append(" more\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 检查统计数据是否在指定时间范围内
     */
    private boolean isInPeriod(ErrorStats stats, LocalDateTime start, LocalDateTime end) {
        return stats.getWindowStart() != null && 
               !stats.getWindowStart().isAfter(end) &&
               (stats.getWindowEnd() == null || !stats.getWindowEnd().isBefore(start));
    }
    
    /**
     * 合并统计数据
     */
    private void mergeStats(ErrorStats target, ErrorStats source) {
        target.setTotalErrors(target.getTotalErrors() + source.getTotalErrors());
        target.setTotalRequests(target.getTotalRequests() + source.getTotalRequests());
        
        // 合并错误类型统计
        source.getErrorsByType().forEach((key, value) -> 
                target.getErrorsByType().merge(key, value, Long::sum));
        
        // 合并应用错误统计
        source.getErrorsByApplication().forEach((key, value) -> 
                target.getErrorsByApplication().merge(key, value, Long::sum));
        
        // 合并Channel错误统计
        source.getErrorsByChannel().forEach((key, value) -> 
                target.getErrorsByChannel().merge(key, value, Long::sum));
    }
}