package com.whosly.stars.netty.visualizer.task;

import com.whosly.stars.netty.visualizer.config.NettyDataWebSocketHandler;
import com.whosly.stars.netty.visualizer.service.NettyMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据推送定时任务
 * 
 * @author fengyang
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class DataPushTask {
    
    private final NettyMonitorService monitorService;
    
    /**
     * 每5秒推送一次统计数据
     */
    @Scheduled(fixedRate = 5000)
    public void pushStats() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "stats");
            data.put("stats", monitorService.getMonitorStats());
            
            NettyDataWebSocketHandler.broadcast(data);
        } catch (Exception e) {
            log.error("Error pushing stats data", e);
        }
    }
    
    /**
     * 每10秒推送一次Channel数据
     */
    @Scheduled(fixedRate = 10000)
    public void pushChannelData() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "channels");
            data.put("channels", monitorService.getAllChannels());
            
            NettyDataWebSocketHandler.broadcast(data);
        } catch (Exception e) {
            log.error("Error pushing channel data", e);
        }
    }
}