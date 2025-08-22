package com.yueny.stars.netty.visualizer.service;

import com.yueny.stars.netty.visualizer.model.ChannelInfo;
import com.yueny.stars.netty.visualizer.model.EventLoopInfo;
import com.yueny.stars.netty.visualizer.model.BufferInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
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
    
    /**
     * 获取指定Channel的缓冲区信息
     */
    public BufferInfo getBufferInfo(String channelId) {
        BufferInfo bufferInfo = new BufferInfo();
        bufferInfo.setChannelId(channelId);
        
        // 检查是否有实际的Channel对象
        Channel channel = findChannelById(channelId);
        if (channel != null) {
            // 如果有实际Channel，可以获取更详细的缓冲区信息
            bufferInfo.setCapacity(1024); // 示例值
            bufferInfo.setReadableBytes(0);
            bufferInfo.setWritableBytes(1024);
            bufferInfo.setReaderIndex(0);
            bufferInfo.setWriterIndex(0);
            bufferInfo.setDirect(true);
            bufferInfo.setRefCount(1);
            bufferInfo.setContent("No data available");
        } else {
            // 对于通过监控代理接收的Channel，提供基本信息
            ChannelInfo channelInfo = channelStats.get(channelId);
            if (channelInfo != null) {
                bufferInfo.setCapacity(1024); // 默认值
                bufferInfo.setReadableBytes((int) channelInfo.getBytesRead());
                bufferInfo.setWritableBytes(1024 - (int) channelInfo.getBytesWritten());
                bufferInfo.setReaderIndex(0);
                bufferInfo.setWriterIndex((int) channelInfo.getBytesWritten());
                bufferInfo.setDirect(true);
                bufferInfo.setRefCount(1);
                bufferInfo.setContent("Remote channel - limited buffer info available");
            } else {
                // 如果找不到Channel信息，返回默认值
                bufferInfo.setCapacity(0);
                bufferInfo.setReadableBytes(0);
                bufferInfo.setWritableBytes(0);
                bufferInfo.setReaderIndex(0);
                bufferInfo.setWriterIndex(0);
                bufferInfo.setDirect(false);
                bufferInfo.setRefCount(0);
                bufferInfo.setContent("Channel not found");
            }
        }
        
        return bufferInfo;
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
            log.info("Channel registered for monitoring: {} from {}", 
                    channelInfo.getChannelId(), channelInfo.getApplicationName());
        }
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