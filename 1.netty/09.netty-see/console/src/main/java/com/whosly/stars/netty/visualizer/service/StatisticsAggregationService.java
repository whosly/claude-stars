package com.whosly.stars.netty.visualizer.service;

import com.whosly.stars.netty.visualizer.model.ChannelInfo;
import com.whosly.stars.netty.visualizer.model.BufferInfo;
import com.whosly.stars.netty.visualizer.model.statistics.TimeWindowStats;
import com.whosly.stars.netty.visualizer.model.statistics.ApplicationStats;
import com.whosly.stars.netty.visualizer.model.statistics.EventLoopStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 统计数据聚合服务
 * 负责收集、聚合和计算各种维度的统计数据
 * 
 * @author fengyang
 */
@Slf4j
@Service
public class StatisticsAggregationService {

    // 时间窗口统计 - 使用不同的时间粒度
    private final Map<String, TimeWindowStats> minuteWindows = new ConcurrentHashMap<>();
    private final Map<String, TimeWindowStats> hourWindows = new ConcurrentHashMap<>();
    private final Map<String, TimeWindowStats> dayWindows = new ConcurrentHashMap<>();

    // 应用维度统计
    private final Map<String, ApplicationStats> applicationStats = new ConcurrentHashMap<>();

    // EventLoop维度统计
    private final Map<String, EventLoopStats> eventLoopStats = new ConcurrentHashMap<>();

    // 实时统计
    private final TimeWindowStats realTimeStats = new TimeWindowStats(
            LocalDateTime.now().minusMinutes(1), LocalDateTime.now());

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    // 统计数据保留策略
    private static final int MAX_MINUTE_WINDOWS = 60; // 保留60分钟
    private static final int MAX_HOUR_WINDOWS = 24; // 保留24小时
    private static final int MAX_DAY_WINDOWS = 30; // 保留30天

    // 性能监控
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private final Map<String, Double> responseTimeBuffer = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing Statistics Aggregation Service...");

        // 启动定时聚合任务
        startAggregationTasks();

        // 启动清理任务
        startCleanupTasks();

        log.info("Statistics Aggregation Service initialized successfully");
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down Statistics Aggregation Service...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Statistics Aggregation Service shut down completed");
    }

    /**
     * 启动聚合任务
     */
    private void startAggregationTasks() {
        // 每分钟聚合任务
        scheduler.scheduleAtFixedRate(this::aggregateMinuteStats, 1, 1, TimeUnit.MINUTES);

        // 每小时聚合任务
        scheduler.scheduleAtFixedRate(this::aggregateHourStats, 1, 1, TimeUnit.HOURS);

        // 每天聚合任务
        scheduler.scheduleAtFixedRate(this::aggregateDayStats, 1, 1, TimeUnit.DAYS);

        log.info("Aggregation tasks started");
    }

    /**
     * 启动清理任务
     */
    private void startCleanupTasks() {
        // 每小时清理过期数据
        scheduler.scheduleAtFixedRate(this::cleanupExpiredData, 1, 1, TimeUnit.HOURS);

        log.info("Cleanup tasks started");
    }

    /**
     * 处理Channel事件
     */
    public void processChannelEvent(ChannelInfo channelInfo, String eventType) {
        try {
            String applicationName = channelInfo.getApplicationName() != null ? channelInfo.getApplicationName()
                    : "Unknown";
            String eventLoopType = channelInfo.getEventLoopGroup() != null ? channelInfo.getEventLoopGroup()
                    : "Unknown";

            // 更新实时统计
            updateRealTimeStats(channelInfo, eventType);

            // 更新应用统计
            updateApplicationStats(channelInfo, eventType, applicationName);

            // 更新EventLoop统计
            updateEventLoopStats(channelInfo, eventType, eventLoopType);

            // 记录性能指标
            recordPerformanceMetrics(channelInfo, eventType);

            log.debug("Processed channel event: {} for channel: {} from app: {}",
                    eventType, channelInfo.getChannelId(), applicationName);

        } catch (Exception e) {
            log.error("Error processing channel event: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理缓冲区事件
     */
    public void processBufferEvent(BufferInfo bufferInfo) {
        try {
            String applicationName = bufferInfo.getApplicationName() != null ? bufferInfo.getApplicationName()
                    : "Unknown";

            // 更新实时统计
            realTimeStats.recordBufferAllocation(bufferInfo.getCapacity());
            realTimeStats.updateBufferUtilization(bufferInfo.getMemoryUtilization());

            // 更新应用统计
            ApplicationStats appStats = getOrCreateApplicationStats(applicationName);
            appStats.recordBufferUsage(
                    bufferInfo.getTotalAllocations(),
                    bufferInfo.getUsedMemory(),
                    bufferInfo.getMemoryUtilization());

            log.debug("Processed buffer event for channel: {} from app: {}",
                    bufferInfo.getChannelId(), applicationName);

        } catch (Exception e) {
            log.error("Error processing buffer event: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新实时统计
     */
    private void updateRealTimeStats(ChannelInfo channelInfo, String eventType) {
        switch (eventType) {
            case "CHANNEL_ACTIVE":
                realTimeStats.recordNewConnection();
                break;
            case "CHANNEL_INACTIVE":
                realTimeStats.recordConnectionClosed();
                break;
            case "CHANNEL_READ":
                realTimeStats.recordBytesRead(channelInfo.getBytesRead());
                realTimeStats.recordMessageRead();
                break;
            case "CHANNEL_WRITE":
                realTimeStats.recordBytesWritten(channelInfo.getBytesWritten());
                realTimeStats.recordMessageWritten();
                break;
            case "CHANNEL_EXCEPTION":
                realTimeStats.recordError();
                break;
        }
    }

    /**
     * 更新应用统计
     */
    private void updateApplicationStats(ChannelInfo channelInfo, String eventType, String applicationName) {
        ApplicationStats appStats = getOrCreateApplicationStats(applicationName);

        switch (eventType) {
            case "CHANNEL_ACTIVE":
                appStats.recordNewConnection(channelInfo.getRemoteAddress());
                break;
            case "CHANNEL_INACTIVE":
                appStats.recordConnectionClosed();
                break;
            case "CHANNEL_READ":
            case "CHANNEL_WRITE":
                // 记录数据传输
                appStats.recordDataTransfer(
                        channelInfo.getBytesRead(),
                        channelInfo.getBytesWritten(),
                        channelInfo.getMessagesRead() + channelInfo.getMessagesWritten());
                break;
            case "CHANNEL_EXCEPTION":
                String errorType = channelInfo.getErrorType() != null ? channelInfo.getErrorType() : "UnknownError";
                appStats.recordError(errorType);
                break;
        }

        // 记录EventLoop使用
        if (channelInfo.getEventLoopGroup() != null) {
            appStats.recordEventLoopUsage(channelInfo.getEventLoopGroup());
        }
    }

    /**
     * 更新EventLoop统计
     */
    private void updateEventLoopStats(ChannelInfo channelInfo, String eventType, String eventLoopType) {
        EventLoopStats elStats = getOrCreateEventLoopStats(eventLoopType);

        String applicationName = channelInfo.getApplicationName() != null ? channelInfo.getApplicationName()
                : "Unknown";
        String channelType = determineChannelType(channelInfo);

        switch (eventType) {
            case "CHANNEL_ACTIVE":
                elStats.recordChannelRegistered(channelInfo.getChannelId(), applicationName, channelType);
                break;
            case "CHANNEL_INACTIVE":
                elStats.recordChannelUnregistered(channelInfo.getChannelId(), applicationName, channelType);
                break;
            case "CHANNEL_READ":
            case "CHANNEL_WRITE":
                elStats.recordDataProcessed(
                        channelInfo.getBytesRead() + channelInfo.getBytesWritten(),
                        channelInfo.getMessagesRead() + channelInfo.getMessagesWritten());
                break;
            case "CHANNEL_EXCEPTION":
                String errorType = channelInfo.getErrorType() != null ? channelInfo.getErrorType() : "UnknownError";
                elStats.recordChannelError(errorType);
                break;
        }
    }

    /**
     * 记录性能指标
     */
    private void recordPerformanceMetrics(ChannelInfo channelInfo, String eventType) {
        String channelId = channelInfo.getChannelId();
        long currentTime = System.currentTimeMillis();

        // 改进的请求-响应配对逻辑
        switch (eventType) {
            case "CHANNEL_READ":
                // 读取事件可能是请求的开始
                lastRequestTime.put(channelId, currentTime);
                // 记录一个请求（不包含响应时间）
                realTimeStats.recordRequest();
                break;
                
            case "CHANNEL_WRITE":
                // 写入事件可能是响应的发送
                Long requestTime = lastRequestTime.remove(channelId); // 移除以避免重复计算
                if (requestTime != null) {
                    double responseTime = currentTime - requestTime;
                    responseTimeBuffer.put(channelId, responseTime);

                    // 记录到实时统计（包含响应时间）
                    realTimeStats.recordRequest(responseTime);
                    realTimeStats.recordSuccessfulRequest();

                    // 记录到应用统计
                    String applicationName = channelInfo.getApplicationName() != null ? 
                            channelInfo.getApplicationName() : "Unknown";
                    ApplicationStats appStats = getOrCreateApplicationStats(applicationName);
                    appStats.recordRequest(responseTime, true);
                    
                    log.debug("Recorded successful request for channel {} with response time {}ms", 
                            channelId, responseTime);
                } else {
                    // 没有对应的读取事件，可能是单独的写入操作
                    realTimeStats.recordRequest();
                    realTimeStats.recordSuccessfulRequest();
                }
                break;
                
            case "CHANNEL_EXCEPTION":
                // 异常事件，记录错误
                realTimeStats.recordError();
                
                // 如果有未完成的请求，也要记录为请求
                Long pendingRequestTime = lastRequestTime.remove(channelId);
                if (pendingRequestTime != null) {
                    double responseTime = currentTime - pendingRequestTime;
                    realTimeStats.recordRequest(responseTime);
                }
                
                // 记录到应用统计
                String applicationName = channelInfo.getApplicationName() != null ? 
                        channelInfo.getApplicationName() : "Unknown";
                ApplicationStats appStats = getOrCreateApplicationStats(applicationName);
                appStats.recordRequest(0, false); // 错误请求，响应时间设为0
                
                log.debug("Recorded error for channel {}", channelId);
                break;
        }
    }

    /**
     * 获取或创建应用统计
     */
    private ApplicationStats getOrCreateApplicationStats(String applicationName) {
        return applicationStats.computeIfAbsent(applicationName, ApplicationStats::new);
    }

    /**
     * 获取或创建EventLoop统计
     */
    private EventLoopStats getOrCreateEventLoopStats(String eventLoopType) {
        return eventLoopStats.computeIfAbsent(eventLoopType,
                name -> new EventLoopStats(name, name));
    }

    /**
     * 确定Channel类型
     */
    private String determineChannelType(ChannelInfo channelInfo) {
        if (channelInfo.getChannelRole() != null) {
            return channelInfo.getChannelRole();
        }

        // 基于地址信息判断
        String localAddr = channelInfo.getLocalAddress();
        String remoteAddr = channelInfo.getRemoteAddress();

        if (localAddr != null && localAddr.contains(":8080")) {
            return "SERVER";
        } else if (remoteAddr != null && remoteAddr.contains(":8080")) {
            return "CLIENT";
        }

        return "UNKNOWN";
    }

    /**
     * 分钟级聚合
     */
    private void aggregateMinuteStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime windowEnd = windowStart.plusMinutes(1);

            String windowKey = windowStart.toString();

            // 创建新的分钟窗口
            TimeWindowStats minuteWindow = new TimeWindowStats(windowStart, windowEnd);

            // 从实时统计复制数据
            minuteWindow.merge(realTimeStats);

            // 存储分钟窗口
            minuteWindows.put(windowKey, minuteWindow);

            // 重置实时统计
            realTimeStats.reset();

            log.debug("Aggregated minute stats for window: {}", windowKey);

        } catch (Exception e) {
            log.error("Error aggregating minute stats: {}", e.getMessage(), e);
        }
    }

    /**
     * 小时级聚合
     */
    private void aggregateHourStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.truncatedTo(ChronoUnit.HOURS);
            LocalDateTime windowEnd = windowStart.plusHours(1);

            String windowKey = windowStart.toString();

            // 创建新的小时窗口
            TimeWindowStats hourWindow = new TimeWindowStats(windowStart, windowEnd);

            // 聚合过去60分钟的数据
            LocalDateTime minuteStart = windowStart;
            for (int i = 0; i < 60; i++) {
                String minuteKey = minuteStart.plusMinutes(i).toString();
                TimeWindowStats minuteStats = minuteWindows.get(minuteKey);
                if (minuteStats != null) {
                    hourWindow.merge(minuteStats);
                }
            }

            // 存储小时窗口
            hourWindows.put(windowKey, hourWindow);

            log.debug("Aggregated hour stats for window: {}", windowKey);

        } catch (Exception e) {
            log.error("Error aggregating hour stats: {}", e.getMessage(), e);
        }
    }

    /**
     * 天级聚合
     */
    private void aggregateDayStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.truncatedTo(ChronoUnit.DAYS);
            LocalDateTime windowEnd = windowStart.plusDays(1);

            String windowKey = windowStart.toString();

            // 创建新的天窗口
            TimeWindowStats dayWindow = new TimeWindowStats(windowStart, windowEnd);

            // 聚合过去24小时的数据
            LocalDateTime hourStart = windowStart;
            for (int i = 0; i < 24; i++) {
                String hourKey = hourStart.plusHours(i).toString();
                TimeWindowStats hourStats = hourWindows.get(hourKey);
                if (hourStats != null) {
                    dayWindow.merge(hourStats);
                }
            }

            // 存储天窗口
            dayWindows.put(windowKey, dayWindow);

            log.debug("Aggregated day stats for window: {}", windowKey);

        } catch (Exception e) {
            log.error("Error aggregating day stats: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理过期数据
     */
    private void cleanupExpiredData() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 清理过期的分钟窗口
            LocalDateTime minuteThreshold = now.minusMinutes(MAX_MINUTE_WINDOWS);
            minuteWindows.entrySet().removeIf(entry -> {
                LocalDateTime windowTime = LocalDateTime.parse(entry.getKey());
                return windowTime.isBefore(minuteThreshold);
            });

            // 清理过期的小时窗口
            LocalDateTime hourThreshold = now.minusHours(MAX_HOUR_WINDOWS);
            hourWindows.entrySet().removeIf(entry -> {
                LocalDateTime windowTime = LocalDateTime.parse(entry.getKey());
                return windowTime.isBefore(hourThreshold);
            });

            // 清理过期的天窗口
            LocalDateTime dayThreshold = now.minusDays(MAX_DAY_WINDOWS);
            dayWindows.entrySet().removeIf(entry -> {
                LocalDateTime windowTime = LocalDateTime.parse(entry.getKey());
                return windowTime.isBefore(dayThreshold);
            });

            // 清理过期的性能数据
            responseTimeBuffer.clear();

            log.debug("Cleaned up expired statistics data");

        } catch (Exception e) {
            log.error("Error cleaning up expired data: {}", e.getMessage(), e);
        }
    }

    // ==================== 查询接口 ====================

    /**
     * 检查数据是否可用
     */
    public boolean isDataAvailable() {
        try {
            // 检查实时统计是否有数据
            if (realTimeStats.getTotalRequests().get() > 0 || 
                realTimeStats.getActiveConnections().get() > 0) {
                return true;
            }
            
            // 检查是否有历史数据
            if (!minuteWindows.isEmpty() || !applicationStats.isEmpty()) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error checking data availability: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取数据源状态
     */
    public Map<String, Object> getDataSourceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("serviceAvailable", true);
            status.put("hasRealTimeData", realTimeStats.getTotalRequests().get() > 0);
            status.put("minuteWindows", minuteWindows.size());
            status.put("hourWindows", hourWindows.size());
            status.put("dayWindows", dayWindows.size());
            status.put("applicationCount", applicationStats.size());
            status.put("eventLoopCount", eventLoopStats.size());
            status.put("lastUpdateTime", LocalDateTime.now());
            
            // 检查数据一致性
            boolean dataConsistent = realTimeStats.isDataConsistent();
            status.put("dataConsistent", dataConsistent);
            
            if (!dataConsistent) {
                status.put("warning", "Data consistency check failed");
            }
            
        } catch (Exception e) {
            status.put("serviceAvailable", false);
            status.put("error", e.getMessage());
            log.error("Error getting data source status: {}", e.getMessage(), e);
        }
        
        return status;
    }

    /**
     * 获取实时统计
     */
    public TimeWindowStats.StatsSummary getRealTimeStats() {
        realTimeStats.calculateThroughputMetrics();
        
        // 验证数据一致性
        if (!realTimeStats.isDataConsistent()) {
            log.warn("Real-time statistics data consistency check failed");
        }
        
        return realTimeStats.getSummary();
    }

    /**
     * 获取指定时间范围的统计
     */
    public List<TimeWindowStats.StatsSummary> getTimeRangeStats(
            LocalDateTime start, LocalDateTime end, String granularity) {

        List<TimeWindowStats.StatsSummary> results = new ArrayList<>();

        switch (granularity.toLowerCase()) {
            case "minute":
                results = getMinuteRangeStats(start, end);
                break;
            case "hour":
                results = getHourRangeStats(start, end);
                break;
            case "day":
                results = getDayRangeStats(start, end);
                break;
            default:
                log.warn("Unknown granularity: {}, using minute", granularity);
                results = getMinuteRangeStats(start, end);
        }

        return results;
    }

    /**
     * 获取分钟级统计
     */
    private List<TimeWindowStats.StatsSummary> getMinuteRangeStats(LocalDateTime start, LocalDateTime end) {
        return minuteWindows.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime windowTime = LocalDateTime.parse(entry.getKey());
                    return !windowTime.isBefore(start) && !windowTime.isAfter(end);
                })
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().getSummary())
                .collect(Collectors.toList());
    }

    /**
     * 获取小时级统计
     */
    private List<TimeWindowStats.StatsSummary> getHourRangeStats(LocalDateTime start, LocalDateTime end) {
        return hourWindows.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime windowTime = LocalDateTime.parse(entry.getKey());
                    return !windowTime.isBefore(start) && !windowTime.isAfter(end);
                })
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().getSummary())
                .collect(Collectors.toList());
    }

    /**
     * 获取天级统计
     */
    private List<TimeWindowStats.StatsSummary> getDayRangeStats(LocalDateTime start, LocalDateTime end) {
        return dayWindows.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime windowTime = LocalDateTime.parse(entry.getKey());
                    return !windowTime.isBefore(start) && !windowTime.isAfter(end);
                })
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().getSummary())
                .collect(Collectors.toList());
    }

    /**
     * 获取所有应用统计
     */
    public List<ApplicationStats.ApplicationStatsSummary> getAllApplicationStats() {
        return applicationStats.values().stream()
                .map(ApplicationStats::getSummary)
                .sorted(Comparator.comparing(ApplicationStats.ApplicationStatsSummary::getTotalConnections).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取指定应用统计
     */
    public ApplicationStats.ApplicationStatsSummary getApplicationStats(String applicationName) {
        ApplicationStats stats = applicationStats.get(applicationName);
        return stats != null ? stats.getSummary() : null;
    }

    /**
     * 获取所有EventLoop统计
     */
    public List<EventLoopStats.EventLoopStatsSummary> getAllEventLoopStats() {
        return eventLoopStats.values().stream()
                .map(EventLoopStats::getSummary)
                .sorted(Comparator.comparing(EventLoopStats.EventLoopStatsSummary::getCurrentChannels).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取指定EventLoop统计
     */
    public EventLoopStats.EventLoopStatsSummary getEventLoopStats(String eventLoopName) {
        EventLoopStats stats = eventLoopStats.get(eventLoopName);
        return stats != null ? stats.getSummary() : null;
    }

    /**
     * 获取统计概览
     */
    public Map<String, Object> getStatisticsOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 实时统计
        TimeWindowStats.StatsSummary realTime = getRealTimeStats();
        overview.put("realTimeStats", realTime);

        // 应用统计概览
        List<ApplicationStats.ApplicationStatsSummary> appStats = getAllApplicationStats();
        overview.put("totalApplications", appStats.size());
        overview.put("topApplications", appStats.stream().limit(5).collect(Collectors.toList()));

        // EventLoop统计概览
        List<EventLoopStats.EventLoopStatsSummary> elStats = getAllEventLoopStats();
        overview.put("totalEventLoops", elStats.size());
        overview.put("overloadedEventLoops", elStats.stream()
                .filter(EventLoopStats.EventLoopStatsSummary::isOverloaded)
                .count());

        // 数据窗口统计
        overview.put("minuteWindows", minuteWindows.size());
        overview.put("hourWindows", hourWindows.size());
        overview.put("dayWindows", dayWindows.size());

        return overview;
    }

    /**
     * 重置所有统计数据
     */
    public void resetAllStats() {
        realTimeStats.reset();
        minuteWindows.clear();
        hourWindows.clear();
        dayWindows.clear();
        applicationStats.values().forEach(ApplicationStats::reset);
        eventLoopStats.values().forEach(EventLoopStats::reset);
        responseTimeBuffer.clear();
        lastRequestTime.clear();

        log.info("All statistics data has been reset");
    }
}