package com.yueny.stars.netty.monitor.agent;

import com.yueny.stars.netty.monitor.agent.model.ChannelInfo;
import com.yueny.stars.netty.monitor.agent.util.JsonUtil;
import com.yueny.stars.netty.monitor.agent.util.Logger;
import io.netty.channel.Channel;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 轻量级监控代理
 * 
 * @author fengyang
 */
public class MonitorAgent {
    
    private static final Logger logger = Logger.getLogger(MonitorAgent.class);
    private static final String DEFAULT_MONITOR_URL = "http://localhost:8080/api/netty/monitor/agent";
    private static MonitorAgent instance;
    
    private final String monitorUrl;
    private final Map<String, ChannelInfo> channels;
    private final ScheduledExecutorService scheduler;
    private String applicationName;
    private boolean enabled = true;
    
    private MonitorAgent(String monitorUrl) {
        this.monitorUrl = monitorUrl;
        this.channels = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.applicationName = "Unknown";
    }
    
    public static synchronized MonitorAgent getInstance() {
        if (instance == null) {
            instance = new MonitorAgent(DEFAULT_MONITOR_URL);
        }
        return instance;
    }
    
    public static synchronized MonitorAgent getInstance(String monitorUrl) {
        if (instance == null) {
            instance = new MonitorAgent(monitorUrl);
        }
        return instance;
    }
    
    public void initialize(String applicationName) {
        this.applicationName = applicationName;
        
        // 每15秒发送一次数据（降低频率减少网络开销）
        scheduler.scheduleAtFixedRate(this::sendMonitorData, 15, 15, TimeUnit.SECONDS);
        
        logger.info("Monitor agent initialized for application: %s", applicationName);
    }
    
    public void registerChannel(Channel channel) {
        if (channel == null || !enabled) return;
        
        ChannelInfo info = new ChannelInfo();
        info.setChannelId(channel.id().asShortText());
        info.setRemoteAddress(channel.remoteAddress() != null ? channel.remoteAddress().toString() : "N/A");
        info.setLocalAddress(channel.localAddress() != null ? channel.localAddress().toString() : "N/A");
        info.setActive(channel.isActive());
        info.setOpen(channel.isOpen());
        info.setState(getChannelState(channel));
        info.setCreateTime(System.currentTimeMillis());
        info.setLastActiveTime(System.currentTimeMillis());
        info.setEventLoopGroup(channel.eventLoop().getClass().getSimpleName());
        info.setPipeline(getPipelineInfo(channel));
        
        channels.put(info.getChannelId(), info);
        logger.debug("Channel registered: %s", info.getChannelId());
    }
    
    public void unregisterChannel(String channelId) {
        if (!enabled) return;
        channels.remove(channelId);
        logger.debug("Channel unregistered: %s", channelId);
    }
    
    public void updateChannel(Channel channel) {
        if (channel == null || !enabled) return;
        
        String channelId = channel.id().asShortText();
        ChannelInfo info = channels.get(channelId);
        if (info != null) {
            info.setActive(channel.isActive());
            info.setOpen(channel.isOpen());
            info.setState(getChannelState(channel));
            if (channel.isActive()) {
                info.setLastActiveTime(System.currentTimeMillis());
            }
        }
    }
    
    private void sendMonitorData() {
        if (!enabled || channels.isEmpty()) return;
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("applicationName", applicationName);
            data.put("timestamp", System.currentTimeMillis());
            data.put("channels", channels.values());
            data.put("stats", getStats());
            
            String jsonData = JsonUtil.toJson(data);
            
            URL url = new URL(monitorUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            // 5秒连接超时
            conn.setConnectTimeout(5000);
            // 5秒读取超时
            conn.setReadTimeout(5000);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonData.getBytes());
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                logger.debug("Monitor data sent successfully");
            } else {
                logger.warn("Failed to send monitor data, response code: %d", responseCode);
            }
            
        } catch (Exception e) {
            logger.debug("Error sending monitor data (will retry): %s", e.getMessage());
            // 不打印完整堆栈，避免日志污染
        }
    }
    
    private Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChannels", channels.size());
        stats.put("activeChannels", channels.values().stream().mapToLong(ch -> ch.isActive() ? 1 : 0).sum());
        return stats;
    }
    
    private String getChannelState(Channel channel) {
        if (!channel.isOpen()) {
            return "CLOSED";
        } else if (!channel.isActive()) {
            return "INACTIVE";
        } else {
            return "ACTIVE";
        }
    }
    
    private String getPipelineInfo(Channel channel) {
        try {
            StringBuilder sb = new StringBuilder();
            channel.pipeline().forEach(entry -> {
                if (sb.length() > 0) {
                    sb.append(" -> ");
                }
                sb.append(entry.getKey()).append("(").append(entry.getValue().getClass().getSimpleName()).append(")");
            });
            return sb.toString();
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Monitor agent shutdown");
    }
}