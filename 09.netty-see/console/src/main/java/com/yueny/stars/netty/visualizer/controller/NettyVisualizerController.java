package com.yueny.stars.netty.visualizer.controller;

import com.yueny.stars.netty.visualizer.model.ChannelInfo;
import com.yueny.stars.netty.visualizer.model.EventLoopInfo;
import com.yueny.stars.netty.visualizer.model.BufferInfo;
import com.yueny.stars.netty.visualizer.service.NettyMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Netty可视化REST API控制器
 * 
 * @author fengyang
 */
@Slf4j
@RestController
@RequestMapping("/api/netty")
@RequiredArgsConstructor
public class NettyVisualizerController {
    
    private final NettyMonitorService monitorService;
    
    /**
     * 获取所有Channel信息
     */
    @GetMapping("/channels")
    public List<ChannelInfo> getAllChannels() {
        return monitorService.getAllChannels();
    }
    
    /**
     * 获取EventLoop信息
     */
    @GetMapping("/eventloops")
    public List<EventLoopInfo> getEventLoops() {
        return monitorService.getEventLoopInfo();
    }
    
    /**
     * 获取指定Channel的缓冲区信息
     */
    @GetMapping("/channels/{channelId}/buffer")
    public BufferInfo getChannelBuffer(@PathVariable String channelId) {
        return monitorService.getBufferInfo(channelId);
    }
    
    /**
     * 获取监控统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return monitorService.getMonitorStats();
    }
    
    /**
     * 移除Channel监控
     */
    @DeleteMapping("/channels/{channelId}")
    public void removeChannel(@PathVariable String channelId) {
        monitorService.unregisterChannel(channelId);
    }
    
    /**
     * 创建测试连接
     */
    @PostMapping("/test/connections")
    public Map<String, Object> createTestConnections(@RequestParam(defaultValue = "1") int count) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 这里可以创建测试连接的逻辑
            result.put("success", true);
            result.put("message", "测试连接创建功能需要客户端主动连接到端口9999");
            result.put("instruction", "请使用 telnet localhost 9999 或运行TestClient来创建连接");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建测试连接失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 接收来自监控代理的数据
     */
    @PostMapping("/monitor/agent")
    public Map<String, Object> receiveAgentData(@RequestBody Map<String, Object> agentData) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 处理来自代理的监控数据
            String applicationName = (String) agentData.get("applicationName");
            Long timestamp = (Long) agentData.get("timestamp");
            
            // 这里可以将代理数据集成到监控服务中
            // 暂时只记录日志
            log.info("Received monitor data from application: {} at {}", applicationName, timestamp);
            
            result.put("success", true);
            result.put("message", "Agent data received successfully");
        } catch (Exception e) {
            log.error("Error processing agent data", e);
            result.put("success", false);
            result.put("message", "Failed to process agent data: " + e.getMessage());
        }
        return result;
    }
}