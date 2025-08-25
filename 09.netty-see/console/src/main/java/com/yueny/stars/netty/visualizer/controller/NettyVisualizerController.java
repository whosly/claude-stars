package com.yueny.stars.netty.visualizer.controller;

import com.yueny.stars.netty.visualizer.model.ChannelInfo;
import com.yueny.stars.netty.visualizer.model.EventLoopInfo;
import com.yueny.stars.netty.visualizer.model.BufferInfo;
import com.yueny.stars.netty.visualizer.model.ErrorStats;
import com.yueny.stars.netty.visualizer.service.NettyMonitorService;
import com.yueny.stars.netty.visualizer.service.ErrorStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
public class NettyVisualizerController {
    
    private final NettyMonitorService monitorService;
    private final ErrorStatsService errorStatsService;
    
    public NettyVisualizerController(NettyMonitorService monitorService, 
                                   @Autowired(required = false) ErrorStatsService errorStatsService) {
        this.monitorService = monitorService;
        this.errorStatsService = errorStatsService;
    }
    
    /**
     * 获取所有Channel信息
     */
    @GetMapping("/channels")
    public List<ChannelInfo> getAllChannels() {
        List<ChannelInfo> channels = monitorService.getAllChannels();
        System.out.println("🌐 API /channels returning " + channels.size() + " channels");
        log.info("API /channels called, returning {} channels", channels.size());
        return channels;
    }
    
    /**
     * 获取EventLoop信息
     */
    @GetMapping("/eventloops")
    public List<EventLoopInfo> getEventLoops() {
        try {
            return monitorService.getEventLoopInfo();
        } catch (Exception e) {
            log.error("Failed to get EventLoop info", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取指定Channel的缓冲区信息
     */
    @GetMapping("/channels/{channelId}/buffer")
    public BufferInfo getChannelBuffer(@PathVariable String channelId) {
        try {
            return monitorService.getBufferInfo(channelId);
        } catch (Exception e) {
            log.error("Failed to get buffer info for channel: {}", channelId, e);
            BufferInfo errorInfo = new BufferInfo();
            errorInfo.setChannelId(channelId);
            errorInfo.setContent("Error: " + e.getMessage());
            return errorInfo;
        }
    }
    
    /**
     * 获取监控统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        try {
            return monitorService.getMonitorStats();
        } catch (Exception e) {
            log.error("Failed to get monitor stats", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalChannels", 0);
            errorStats.put("activeChannels", 0);
            errorStats.put("eventLoops", 0);
            errorStats.put("error", e.getMessage());
            return errorStats;
        }
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
    
    /**
     * 获取错误统计信息
     */
    @GetMapping("/errors/stats")
    public ErrorStats getErrorStats() {
        try {
            if (errorStatsService != null) {
                return errorStatsService.getCurrentStats();
            } else {
                log.warn("ErrorStatsService not available");
                return new ErrorStats();
            }
        } catch (Exception e) {
            log.error("Failed to get error stats", e);
            return new ErrorStats();
        }
    }
    
    /**
     * 获取错误趋势数据
     */
    @GetMapping("/errors/trend")
    public List<Map<String, Object>> getErrorTrend(@RequestParam(defaultValue = "24") int hours) {
        try {
            if (errorStatsService != null) {
                return errorStatsService.getErrorTrend(hours);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to get error trend", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取Top错误类型
     */
    @GetMapping("/errors/top-types")
    public List<Map<String, Object>> getTopErrorTypes(@RequestParam(defaultValue = "10") int limit) {
        try {
            if (errorStatsService != null) {
                return errorStatsService.getTopErrorTypes(limit);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to get top error types", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取Top错误应用
     */
    @GetMapping("/errors/top-applications")
    public List<Map<String, Object>> getTopErrorApplications(@RequestParam(defaultValue = "10") int limit) {
        try {
            if (errorStatsService != null) {
                return errorStatsService.getTopErrorApplications(limit);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to get top error applications", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取最近的错误记录
     */
    @GetMapping("/errors/recent")
    public List<ErrorStats.ErrorRecord> getRecentErrors(@RequestParam(defaultValue = "20") int limit) {
        try {
            if (errorStatsService != null) {
                return errorStatsService.getRecentErrors(limit);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to get recent errors", e);
            return new ArrayList<>();
        }
    }
}