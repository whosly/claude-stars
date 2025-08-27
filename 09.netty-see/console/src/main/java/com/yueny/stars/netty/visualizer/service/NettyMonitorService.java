package com.yueny.stars.netty.visualizer.service;

import com.yueny.stars.netty.visualizer.model.ChannelInfo;
import com.yueny.stars.netty.visualizer.model.EventLoopInfo;
import com.yueny.stars.netty.visualizer.model.BufferInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Netty监控服务
 * 
 * @author fengyang
 */
@Slf4j
@Service
public class NettyMonitorService {
    
    // 存储所有被监控的Channel
    private final ChannelGroup monitoredChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    // 存储Channel的统计信息
    private final Map<String, ChannelInfo> channelStats = new ConcurrentHashMap<>();
    
    // 存储EventLoop信息
    private final Map<String, EventLoopInfo> eventLoopStats = new ConcurrentHashMap<>();
    
    // 定时清理任务
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // 错误统计服务 - 使用可选依赖避免启动问题
    @Autowired(required = false)
    private ErrorStatsService errorStatsService;
    
    // 统计聚合服务
    @Autowired(required = false)
    private StatisticsAggregationService statisticsService;
    
    /**
     * 注册Channel进行监控
     */
    public void registerChannel(Channel channel) {
        if (channel != null) {
            monitoredChannels.add(channel);
            
            ChannelInfo info = new ChannelInfo();
            info.setChannelId(channel.id().asShortText());
            info.setRemoteAddress(channel.remoteAddress() != null ? channel.remoteAddress().toString() : "N/A");
            info.setLocalAddress(channel.localAddress() != null ? channel.localAddress().toString() : "N/A");
            info.setActive(channel.isActive());
            info.setOpen(channel.isOpen());
            info.setState(getChannelState(channel));
            info.setCreateTime(LocalDateTime.now());
            info.setLastActiveTime(LocalDateTime.now());
            info.setEventLoopGroup(channel.eventLoop().getClass().getSimpleName());
            info.setPipeline(getPipelineInfo(channel.pipeline()));
            
            channelStats.put(info.getChannelId(), info);
            
            log.info("Channel registered for monitoring: {}", info.getChannelId());
        }
    }
    
    /**
     * 获取所有Channel信息
     */
    public List<ChannelInfo> getAllChannels() {
        // 更新Channel状态
        updateChannelStats();
        // 清理过期的Channel
        cleanupExpiredChannels();
        return new ArrayList<>(channelStats.values());
    }
    
    /**
     * 获取EventLoop信息
     */
    public List<EventLoopInfo> getEventLoopInfo() {
        updateEventLoopStats();
        
        // 同时从channelStats中收集EventLoop信息
        Map<String, EventLoopInfo> allEventLoops = new HashMap<>(eventLoopStats);
        
        // 从监控代理接收的Channel中提取EventLoop信息
        Map<String, List<String>> eventLoopChannels = new HashMap<>();
        for (ChannelInfo channelInfo : channelStats.values()) {
            if (channelInfo.getEventLoopGroup() != null) {
                String eventLoopType = channelInfo.getEventLoopGroup();
                eventLoopChannels.computeIfAbsent(eventLoopType, k -> new ArrayList<>())
                        .add(channelInfo.getChannelId());
                
                // 如果这个EventLoop类型还没有记录，创建一个
                if (!allEventLoops.containsKey(eventLoopType)) {
                    EventLoopInfo info = new EventLoopInfo();
                    info.setName(eventLoopType);
                    info.setType(eventLoopType);
                    info.setThreadCount(1); // 默认值
                    info.setTasksExecuted(0);
                    info.setTasksQueued(0);
                    info.setShutdown(false);
                    info.setCpuUsage(0.0);
                    allEventLoops.put(eventLoopType, info);
                }
            }
        }
        
        // 更新Channel列表
        for (Map.Entry<String, List<String>> entry : eventLoopChannels.entrySet()) {
            EventLoopInfo info = allEventLoops.get(entry.getKey());
            if (info != null) {
                List<String> existingChannels = info.getChannels();
                if (existingChannels == null) {
                    info.setChannels(entry.getValue());
                } else {
                    // 合并Channel列表
                    Set<String> allChannels = new HashSet<>(existingChannels);
                    allChannels.addAll(entry.getValue());
                    info.setChannels(new ArrayList<>(allChannels));
                }
            }
        }
        
        return new ArrayList<>(allEventLoops.values());
    }
    
    // 存储缓冲区信息
    private final Map<String, BufferInfo> bufferStats = new ConcurrentHashMap<>();
    
    /**
     * 获取所有缓冲区信息
     */
    public List<BufferInfo> getAllBuffers() {
        // 更新缓冲区统计信息
        updateBufferStats();
        return new ArrayList<>(bufferStats.values());
    }
    
    /**
     * 获取指定Channel的缓冲区信息
     */
    public BufferInfo getBufferInfo(String channelId) {
        BufferInfo bufferInfo = bufferStats.get(channelId);
        if (bufferInfo != null) {
            // 更新时间戳
            bufferInfo.setLastUpdateTime(LocalDateTime.now());
            return bufferInfo;
        }
        
        // 如果缓存中没有，尝试从Channel创建
        bufferInfo = createBufferInfoFromChannel(channelId);
        if (bufferInfo != null) {
            bufferStats.put(channelId, bufferInfo);
        }
        
        return bufferInfo;
    }
    
    /**
     * 从Channel创建缓冲区信息
     */
    private BufferInfo createBufferInfoFromChannel(String channelId) {
        BufferInfo bufferInfo = new BufferInfo();
        bufferInfo.setChannelId(channelId);
        bufferInfo.setLastUpdateTime(LocalDateTime.now());
        
        // 检查是否有实际的Channel对象
        Channel channel = findChannelById(channelId);
        if (channel != null) {
            // 从实际Channel获取信息
            populateBufferInfoFromChannel(bufferInfo, channel);
        } else {
            // 对于通过监控代理接收的Channel，提供基本信息
            ChannelInfo channelInfo = channelStats.get(channelId);
            if (channelInfo != null) {
                populateBufferInfoFromChannelInfo(bufferInfo, channelInfo);
            } else {
                // 如果找不到Channel信息，返回默认值
                populateDefaultBufferInfo(bufferInfo);
            }
        }
        
        return bufferInfo;
    }
    
    /**
     * 从实际Channel填充缓冲区信息
     */
    private void populateBufferInfoFromChannel(BufferInfo bufferInfo, Channel channel) {
        ChannelInfo channelInfo = channelStats.get(channel.id().asShortText());
        
        // 基本信息
        bufferInfo.setApplicationName(channelInfo != null ? channelInfo.getApplicationName() : "Local");
        bufferInfo.setCapacity(8192); // 默认8KB
        bufferInfo.setMaxCapacity(65536); // 默认64KB
        bufferInfo.setReadableBytes(0);
        bufferInfo.setWritableBytes(8192);
        bufferInfo.setReaderIndex(0);
        bufferInfo.setWriterIndex(0);
        bufferInfo.setDirect(true);
        bufferInfo.setHasArray(false);
        bufferInfo.setRefCount(1);
        bufferInfo.setBufferType("DirectByteBuf");
        bufferInfo.setContent("Active channel - buffer content not accessible");
        
        // 统计信息
        if (channelInfo != null) {
            bufferInfo.setTotalReads(channelInfo.getBytesRead());
            bufferInfo.setTotalWrites(channelInfo.getBytesWritten());
        }
        
        // 计算内存利用率
        bufferInfo.calculateMemoryUtilization();
        bufferInfo.addUsageSnapshot();
    }
    
    /**
     * 从ChannelInfo填充缓冲区信息
     */
    private void populateBufferInfoFromChannelInfo(BufferInfo bufferInfo, ChannelInfo channelInfo) {
        bufferInfo.setApplicationName(channelInfo.getApplicationName());
        
        // 根据传输的数据量估算缓冲区大小
        long totalBytes = channelInfo.getBytesRead() + channelInfo.getBytesWritten();
        int estimatedCapacity = Math.max(1024, (int) Math.min(totalBytes, 65536));
        
        bufferInfo.setCapacity(estimatedCapacity);
        bufferInfo.setMaxCapacity(65536);
        bufferInfo.setReadableBytes((int) Math.min(channelInfo.getBytesRead(), estimatedCapacity));
        bufferInfo.setWritableBytes(estimatedCapacity - (int) Math.min(channelInfo.getBytesWritten(), estimatedCapacity));
        bufferInfo.setReaderIndex(0);
        bufferInfo.setWriterIndex((int) Math.min(channelInfo.getBytesWritten(), estimatedCapacity));
        bufferInfo.setDirect(true);
        bufferInfo.setHasArray(false);
        bufferInfo.setRefCount(1);
        bufferInfo.setBufferType("RemoteByteBuf");
        bufferInfo.setContent("Remote channel - estimated buffer info");
        
        // 统计信息
        bufferInfo.setTotalReads(channelInfo.getBytesRead());
        bufferInfo.setTotalWrites(channelInfo.getBytesWritten());
        bufferInfo.setTotalAllocations(1);
        bufferInfo.setTotalDeallocations(channelInfo.isActive() ? 0 : 1);
        
        // 内存使用情况
        bufferInfo.setUsedMemory(estimatedCapacity - bufferInfo.getWritableBytes());
        bufferInfo.setAllocatedMemory(estimatedCapacity);
        
        // 计算内存利用率
        bufferInfo.calculateMemoryUtilization();
        bufferInfo.addUsageSnapshot();
    }
    
    /**
     * 填充默认缓冲区信息
     */
    private void populateDefaultBufferInfo(BufferInfo bufferInfo) {
        bufferInfo.setApplicationName("Unknown");
        bufferInfo.setCapacity(0);
        bufferInfo.setMaxCapacity(0);
        bufferInfo.setReadableBytes(0);
        bufferInfo.setWritableBytes(0);
        bufferInfo.setReaderIndex(0);
        bufferInfo.setWriterIndex(0);
        bufferInfo.setDirect(false);
        bufferInfo.setHasArray(false);
        bufferInfo.setRefCount(0);
        bufferInfo.setBufferType("Unknown");
        bufferInfo.setContent("Channel not found");
        
        bufferInfo.setTotalReads(0);
        bufferInfo.setTotalWrites(0);
        bufferInfo.setTotalAllocations(0);
        bufferInfo.setTotalDeallocations(0);
        bufferInfo.setUsedMemory(0);
        bufferInfo.setAllocatedMemory(0);
        bufferInfo.setMemoryUtilization(0);
    }
    
    /**
     * 更新缓冲区统计信息
     */
    private void updateBufferStats() {
        // 为所有活跃的Channel创建或更新缓冲区信息
        Set<String> activeChannelIds = new HashSet<>();
        
        // 处理本地Channel
        for (Channel channel : monitoredChannels) {
            String channelId = channel.id().asShortText();
            activeChannelIds.add(channelId);
            
            BufferInfo bufferInfo = bufferStats.get(channelId);
            if (bufferInfo == null) {
                bufferInfo = createBufferInfoFromChannel(channelId);
                if (bufferInfo != null) {
                    bufferStats.put(channelId, bufferInfo);
                }
            } else {
                // 更新现有缓冲区信息
                bufferInfo.setLastUpdateTime(LocalDateTime.now());
                bufferInfo.addUsageSnapshot();
            }
        }
        
        // 处理远程Channel
        for (String channelId : channelStats.keySet()) {
            activeChannelIds.add(channelId);
            
            BufferInfo bufferInfo = bufferStats.get(channelId);
            if (bufferInfo == null) {
                bufferInfo = createBufferInfoFromChannel(channelId);
                if (bufferInfo != null) {
                    bufferStats.put(channelId, bufferInfo);
                }
            } else {
                // 更新现有缓冲区信息
                ChannelInfo channelInfo = channelStats.get(channelId);
                if (channelInfo != null) {
                    populateBufferInfoFromChannelInfo(bufferInfo, channelInfo);
                }
            }
        }
        
        // 清理不活跃的缓冲区信息
        bufferStats.entrySet().removeIf(entry -> {
            String channelId = entry.getKey();
            BufferInfo bufferInfo = entry.getValue();
            
            // 如果Channel不再活跃且超过5分钟没有更新，则移除
            boolean shouldRemove = !activeChannelIds.contains(channelId) &&
                    bufferInfo.getLastUpdateTime() != null &&
                    bufferInfo.getLastUpdateTime().isBefore(LocalDateTime.now().minusMinutes(5));
            
            if (shouldRemove) {
                log.debug("Removed inactive buffer info for channel: {}", channelId);
            }
            
            return shouldRemove;
        });
    }
    
    /**
     * 注册缓冲区信息
     */
    public void registerBufferInfo(String channelId, BufferInfo bufferInfo) {
        if (bufferInfo != null) {
            bufferInfo.setChannelId(channelId);
            bufferInfo.setLastUpdateTime(LocalDateTime.now());
            bufferStats.put(channelId, bufferInfo);
            log.debug("Buffer info registered for channel: {}", channelId);
        }
    }
    
    /**
     * 更新缓冲区使用情况
     */
    public void updateBufferUsage(String channelId, int capacity, int readableBytes, int writableBytes) {
        BufferInfo bufferInfo = bufferStats.get(channelId);
        if (bufferInfo != null) {
            bufferInfo.setCapacity(capacity);
            bufferInfo.setReadableBytes(readableBytes);
            bufferInfo.setWritableBytes(writableBytes);
            bufferInfo.setLastUpdateTime(LocalDateTime.now());
            bufferInfo.calculateMemoryUtilization();
            bufferInfo.addUsageSnapshot();
        }
    }
    
    /**
     * 更新Channel统计信息
     */
    private void updateChannelStats() {
        for (Channel channel : monitoredChannels) {
            String channelId = channel.id().asShortText();
            ChannelInfo info = channelStats.get(channelId);
            if (info != null) {
                info.setActive(channel.isActive());
                info.setOpen(channel.isOpen());
                info.setState(getChannelState(channel));
                if (channel.isActive()) {
                    info.setLastActiveTime(LocalDateTime.now());
                }
            }
        }
    }
    
    /**
     * 更新EventLoop统计信息
     */
    private void updateEventLoopStats() {
        Set<String> processedEventLoops = new HashSet<>();
        
        for (Channel channel : monitoredChannels) {
            String eventLoopName = channel.eventLoop().toString();
            if (!processedEventLoops.contains(eventLoopName)) {
                EventLoopInfo info = eventLoopStats.computeIfAbsent(eventLoopName, k -> new EventLoopInfo());
                info.setName(eventLoopName);
                info.setType(channel.eventLoop().getClass().getSimpleName());
                info.setShutdown(channel.eventLoop().isShutdown());
                
                // 收集该EventLoop下的所有Channel
                List<String> channels = new ArrayList<>();
                for (Channel ch : monitoredChannels) {
                    if (ch.eventLoop().toString().equals(eventLoopName)) {
                        channels.add(ch.id().asShortText());
                    }
                }
                info.setChannels(channels);
                
                processedEventLoops.add(eventLoopName);
            }
        }
    }
    
    /**
     * 根据ID查找Channel
     */
    private Channel findChannelById(String channelId) {
        for (Channel channel : monitoredChannels) {
            if (channel.id().asShortText().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }
    
    /**
     * 获取Channel状态
     */
    private String getChannelState(Channel channel) {
        if (!channel.isOpen()) {
            return "CLOSED";
        } else if (!channel.isActive()) {
            return "INACTIVE";
        } else {
            return "ACTIVE";
        }
    }
    
    /**
     * 获取Pipeline信息
     */
    private String getPipelineInfo(ChannelPipeline pipeline) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            if (sb.length() > 0) {
                sb.append(" -> ");
            }
            sb.append(entry.getKey()).append("(").append(entry.getValue().getClass().getSimpleName()).append(")");
        }
        return sb.toString();
    }
    
    /**
     * 注册Channel进行监控（使用ChannelInfo对象）
     */
    public void registerChannel(ChannelInfo channelInfo) {
        if (channelInfo != null) {
            channelStats.put(channelInfo.getChannelId(), channelInfo);
            
            // 如果包含缓冲区信息，同时更新缓冲区统计
            if (channelInfo.getBufferInfo() != null) {
                updateBufferInfoFromChannelInfo(channelInfo);
            }
            
            log.info("Channel registered for monitoring: {} from {}", 
                    channelInfo.getChannelId(), channelInfo.getApplicationName());
            System.out.println("💾 NettyMonitorService: Stored channel: " + channelInfo.getChannelId() + 
                    " from " + channelInfo.getApplicationName() + " (Total: " + channelStats.size() + ")");
        }
    }
    
    /**
     * 从ChannelInfo更新缓冲区信息
     */
    private void updateBufferInfoFromChannelInfo(ChannelInfo channelInfo) {
        if (channelInfo.getBufferInfo() == null) {
            return;
        }
        
        String channelId = channelInfo.getChannelId();
        Map<String, Object> bufferData = channelInfo.getBufferInfo();
        
        BufferInfo bufferInfo = bufferStats.get(channelId);
        if (bufferInfo == null) {
            bufferInfo = new BufferInfo();
            bufferInfo.setChannelId(channelId);
            bufferStats.put(channelId, bufferInfo);
        }
        
        // 更新缓冲区信息
        bufferInfo.setApplicationName(channelInfo.getApplicationName());
        bufferInfo.setLastUpdateTime(LocalDateTime.now());
        
        // 从Map中提取缓冲区数据
        if (bufferData.containsKey("capacity")) {
            bufferInfo.setCapacity((Integer) bufferData.get("capacity"));
        }
        if (bufferData.containsKey("maxCapacity")) {
            bufferInfo.setMaxCapacity((Integer) bufferData.get("maxCapacity"));
        }
        if (bufferData.containsKey("readableBytes")) {
            bufferInfo.setReadableBytes((Integer) bufferData.get("readableBytes"));
        }
        if (bufferData.containsKey("writableBytes")) {
            bufferInfo.setWritableBytes((Integer) bufferData.get("writableBytes"));
        }
        if (bufferData.containsKey("readerIndex")) {
            bufferInfo.setReaderIndex((Integer) bufferData.get("readerIndex"));
        }
        if (bufferData.containsKey("writerIndex")) {
            bufferInfo.setWriterIndex((Integer) bufferData.get("writerIndex"));
        }
        if (bufferData.containsKey("isDirect")) {
            bufferInfo.setDirect((Boolean) bufferData.get("isDirect"));
        }
        if (bufferData.containsKey("hasArray")) {
            bufferInfo.setHasArray((Boolean) bufferData.get("hasArray"));
        }
        if (bufferData.containsKey("refCount")) {
            bufferInfo.setRefCount((Integer) bufferData.get("refCount"));
        }
        if (bufferData.containsKey("bufferType")) {
            bufferInfo.setBufferType((String) bufferData.get("bufferType"));
        }
        if (bufferData.containsKey("memoryUtilization")) {
            bufferInfo.setMemoryUtilization((Double) bufferData.get("memoryUtilization"));
        }
        if (bufferData.containsKey("contentPreview")) {
            bufferInfo.setContent("Buffer content preview: " + bufferData.get("contentPreview"));
        }
        
        // 更新统计信息
        bufferInfo.setTotalReads(channelInfo.getBytesRead());
        bufferInfo.setTotalWrites(channelInfo.getBytesWritten());
        
        // 计算内存使用情况
        if (bufferInfo.getCapacity() > 0) {
            bufferInfo.setUsedMemory(bufferInfo.getCapacity() - bufferInfo.getWritableBytes());
            bufferInfo.setAllocatedMemory(bufferInfo.getCapacity());
        }
        
        // 添加使用快照
        bufferInfo.addUsageSnapshot();
        
        log.debug("Updated buffer info from channel data: {}", channelId);
    }
    
    /**
     * 更新Channel信息
     */
    public void updateChannelInfo(ChannelInfo channelInfo) {
        if (channelInfo != null) {
            channelStats.put(channelInfo.getChannelId(), channelInfo);
            log.debug("Channel info updated: {}", channelInfo.getChannelId());
        }
    }
    
    /**
     * 处理Channel事件并更新统计
     */
    public void processChannelEvent(ChannelInfo channelInfo, String eventType) {
        // 更新Channel信息
        updateChannelInfo(channelInfo);
        
        // 处理缓冲区信息
        if (channelInfo.getBufferInfo() != null) {
            updateBufferInfoFromChannelInfo(channelInfo);
        }
        
        // 发送到统计聚合服务
        if (statisticsService != null) {
            statisticsService.processChannelEvent(channelInfo, eventType);
        }
        
        // 处理特定事件类型
        switch (eventType) {
            case "CHANNEL_ACTIVE":
                recordChannelSuccess(channelInfo.getChannelId());
                break;
            case "CHANNEL_INACTIVE":
                markChannelClosed(channelInfo.getChannelId());
                break;
            case "CHANNEL_EXCEPTION":
                handleChannelException(channelInfo);
                break;
            case "CHANNEL_READ":
            case "CHANNEL_WRITE":
                recordChannelRequest(channelInfo.getChannelId());
                break;
        }
        
        log.debug("Processed channel event: {} for channel: {}", eventType, channelInfo.getChannelId());
    }
    
    /**
     * 处理Channel异常事件
     */
    public void handleChannelException(ChannelInfo channelInfo) {
        if (channelInfo != null && channelInfo.getErrorMessage() != null) {
            // 创建异常对象用于统计
            Exception exception = new Exception(channelInfo.getErrorMessage());
            if (channelInfo.getErrorType() != null) {
                try {
                    Class<?> exceptionClass = Class.forName("java.lang." + channelInfo.getErrorType());
                    if (Exception.class.isAssignableFrom(exceptionClass)) {
                        exception = (Exception) exceptionClass.getConstructor(String.class)
                                .newInstance(channelInfo.getErrorMessage());
                    }
                } catch (Exception e) {
                    // 使用默认异常
                }
            }
            
            // 记录到错误统计服务
            if (errorStatsService != null) {
                errorStatsService.recordException(
                        channelInfo.getChannelId(),
                        getApplicationNameFromChannel(channelInfo.getChannelId()),
                        exception,
                        channelInfo.getRemoteAddress(),
                        channelInfo.getLocalAddress()
                );
            }
            
            log.warn("Channel exception recorded: {} - {}", 
                    channelInfo.getChannelId(), channelInfo.getErrorMessage());
        }
    }
    
    /**
     * 记录成功的Channel操作
     */
    public void recordChannelSuccess(String channelId) {
        if (errorStatsService != null) {
            errorStatsService.recordSuccess(channelId, getApplicationNameFromChannel(channelId));
        }
    }
    
    /**
     * 记录Channel请求
     */
    public void recordChannelRequest(String channelId) {
        if (errorStatsService != null) {
            errorStatsService.recordRequest(channelId, getApplicationNameFromChannel(channelId));
        }
    }
    
    /**
     * 从Channel ID获取应用名称
     */
    private String getApplicationNameFromChannel(String channelId) {
        ChannelInfo info = channelStats.get(channelId);
        return info != null ? info.getApplicationName() : "Unknown";
    }
    
    /**
     * 移除Channel监控
     */
    public void unregisterChannel(String channelId) {
        Channel channel = findChannelById(channelId);
        if (channel != null) {
            monitoredChannels.remove(channel);
        }
        channelStats.remove(channelId);
        log.info("Channel unregistered from monitoring: {}", channelId);
    }
    
    /**
     * 清理过期的Channel
     */
    private void cleanupExpiredChannels() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredChannels = new ArrayList<>();
        
        for (Map.Entry<String, ChannelInfo> entry : channelStats.entrySet()) {
            ChannelInfo info = entry.getValue();
            
            // 如果Channel已经标记为CLOSED超过30秒，或者超过2分钟没有活动，则认为已过期
            boolean isClosed = "CLOSED".equals(info.getState()) && 
                    info.getLastActiveTime() != null && 
                    info.getLastActiveTime().isBefore(now.minusSeconds(30));
            
            boolean isInactive = info.getLastActiveTime() != null && 
                    info.getLastActiveTime().isBefore(now.minusMinutes(2)) &&
                    !info.isActive();
            
            if (isClosed || isInactive) {
                expiredChannels.add(entry.getKey());
            }
        }
        
        // 移除过期的Channel
        if (!expiredChannels.isEmpty()) {
            for (String channelId : expiredChannels) {
                ChannelInfo info = channelStats.remove(channelId);
                log.info("Removed expired channel: {} from {} (state: {}, lastActive: {})", 
                        channelId, 
                        info.getApplicationName(), 
                        info.getState(), 
                        info.getLastActiveTime());
            }
        }
    }
    
    /**
     * 标记Channel为关闭状态
     */
    public void markChannelClosed(String channelId) {
        ChannelInfo info = channelStats.get(channelId);
        if (info != null) {
            info.setState("CLOSED");
            info.setActive(false);
            info.setOpen(false);
            info.setLastActiveTime(LocalDateTime.now());
            log.info("Channel marked as closed: {}", channelId);
        }
    }
    
    /**
     * 获取监控统计信息
     */
    public Map<String, Object> getMonitorStats() {
        // 先清理过期Channel
        cleanupExpiredChannels();
        
        Map<String, Object> stats = new HashMap<>();
        
        // 统计所有Channel（包括从监控代理接收的）
        int totalChannels = channelStats.size() + monitoredChannels.size();
        long activeChannels = channelStats.values().stream()
                .mapToLong(ch -> ch.isActive() ? 1 : 0).sum() +
                monitoredChannels.stream().mapToLong(ch -> ch.isActive() ? 1 : 0).sum();
        
        // 统计EventLoop（从channelStats中提取）
        Set<String> eventLoops = new HashSet<>();
        channelStats.values().forEach(ch -> {
            if (ch.getEventLoopGroup() != null) {
                eventLoops.add(ch.getEventLoopGroup());
            }
        });
        eventLoops.addAll(eventLoopStats.keySet());
        
        stats.put("totalChannels", totalChannels);
        stats.put("activeChannels", activeChannels);
        stats.put("eventLoops", eventLoops.size());
        
        return stats;
    }
    
    /**
     * 初始化定时清理任务
     */
    @PostConstruct
    public void init() {
        // 每分钟执行一次清理任务
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredChannels, 1, 1, TimeUnit.MINUTES);
        log.info("Channel cleanup task started");
    }
    
    /**
     * 关闭清理任务
     */
    @PreDestroy
    public void destroy() {
        cleanupExecutor.shutdown();
        log.info("Channel cleanup task stopped");
    }
}