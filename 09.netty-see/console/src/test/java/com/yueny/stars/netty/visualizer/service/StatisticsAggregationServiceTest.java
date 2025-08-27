package com.yueny.stars.netty.visualizer.service;

import com.yueny.stars.netty.visualizer.model.ChannelInfo;
import com.yueny.stars.netty.visualizer.model.statistics.TimeWindowStats;
import com.yueny.stars.netty.visualizer.model.statistics.ApplicationStats;
import com.yueny.stars.netty.visualizer.model.statistics.EventLoopStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统计聚合服务测试
 * 
 * @author fengyang
 */
@ExtendWith(MockitoExtension.class)
class StatisticsAggregationServiceTest {
    
    private StatisticsAggregationService statisticsService;
    
    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsAggregationService();
        statisticsService.init();
    }
    
    @Test
    void testProcessChannelActiveEvent() {
        // 准备测试数据
        ChannelInfo channelInfo = createTestChannelInfo("test-channel-1", "TestApp");
        
        // 处理Channel激活事件
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_ACTIVE");
        
        // 验证实时统计
        TimeWindowStats.StatsSummary realTimeStats = statisticsService.getRealTimeStats();
        assertNotNull(realTimeStats);
        assertEquals(1, realTimeStats.getTotalConnections());
        assertEquals(1, realTimeStats.getActiveConnections());
        
        // 验证应用统计
        List<ApplicationStats.ApplicationStatsSummary> appStats = statisticsService.getAllApplicationStats();
        assertEquals(1, appStats.size());
        assertEquals("TestApp", appStats.get(0).getApplicationName());
        assertEquals(1, appStats.get(0).getTotalConnections());
        assertEquals(1, appStats.get(0).getCurrentConnections());
    }
    
    @Test
    void testProcessChannelInactiveEvent() {
        // 先激活一个Channel
        ChannelInfo channelInfo = createTestChannelInfo("test-channel-1", "TestApp");
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_ACTIVE");
        
        // 然后关闭Channel
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_INACTIVE");
        
        // 验证统计数据
        TimeWindowStats.StatsSummary realTimeStats = statisticsService.getRealTimeStats();
        assertEquals(1, realTimeStats.getTotalConnections());
        assertEquals(0, realTimeStats.getActiveConnections()); // 应该减少
        
        ApplicationStats.ApplicationStatsSummary appStats = statisticsService.getApplicationStats("TestApp");
        assertNotNull(appStats);
        assertEquals(1, appStats.getTotalConnections());
        assertEquals(0, appStats.getCurrentConnections());
    }
    
    @Test
    void testProcessChannelReadEvent() {
        // 准备测试数据
        ChannelInfo channelInfo = createTestChannelInfo("test-channel-1", "TestApp");
        channelInfo.setBytesRead(1024);
        channelInfo.setMessagesRead(1);
        
        // 处理读取事件
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_READ");
        
        // 验证统计数据
        TimeWindowStats.StatsSummary realTimeStats = statisticsService.getRealTimeStats();
        assertTrue(realTimeStats.getTotalBytes() > 0);
        
        ApplicationStats.ApplicationStatsSummary appStats = statisticsService.getApplicationStats("TestApp");
        assertNotNull(appStats);
        assertTrue(appStats.getTotalBytes() > 0);
    }
    
    @Test
    void testProcessChannelExceptionEvent() {
        // 准备测试数据
        ChannelInfo channelInfo = createTestChannelInfo("test-channel-1", "TestApp");
        channelInfo.setErrorMessage("Test exception");
        channelInfo.setErrorType("RuntimeException");
        
        // 处理异常事件
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_EXCEPTION");
        
        // 验证错误统计
        TimeWindowStats.StatsSummary realTimeStats = statisticsService.getRealTimeStats();
        assertTrue(realTimeStats.getErrorRate() >= 0);
        
        ApplicationStats.ApplicationStatsSummary appStats = statisticsService.getApplicationStats("TestApp");
        assertNotNull(appStats);
        assertTrue(appStats.getErrorRate() >= 0);
    }
    
    @Test
    void testMultipleApplications() {
        // 创建多个应用的Channel
        ChannelInfo app1Channel = createTestChannelInfo("app1-channel-1", "App1");
        ChannelInfo app2Channel = createTestChannelInfo("app2-channel-1", "App2");
        
        // 处理事件
        statisticsService.processChannelEvent(app1Channel, "CHANNEL_ACTIVE");
        statisticsService.processChannelEvent(app2Channel, "CHANNEL_ACTIVE");
        
        // 验证应用统计
        List<ApplicationStats.ApplicationStatsSummary> appStats = statisticsService.getAllApplicationStats();
        assertEquals(2, appStats.size());
        
        // 验证每个应用的统计
        ApplicationStats.ApplicationStatsSummary app1Stats = statisticsService.getApplicationStats("App1");
        ApplicationStats.ApplicationStatsSummary app2Stats = statisticsService.getApplicationStats("App2");
        
        assertNotNull(app1Stats);
        assertNotNull(app2Stats);
        assertEquals("App1", app1Stats.getApplicationName());
        assertEquals("App2", app2Stats.getApplicationName());
    }
    
    @Test
    void testEventLoopStatistics() {
        // 创建不同EventLoop的Channel
        ChannelInfo channel1 = createTestChannelInfo("channel-1", "TestApp");
        channel1.setEventLoopGroup("NioEventLoop-1");
        
        ChannelInfo channel2 = createTestChannelInfo("channel-2", "TestApp");
        channel2.setEventLoopGroup("NioEventLoop-2");
        
        // 处理事件
        statisticsService.processChannelEvent(channel1, "CHANNEL_ACTIVE");
        statisticsService.processChannelEvent(channel2, "CHANNEL_ACTIVE");
        
        // 验证EventLoop统计
        List<EventLoopStats.EventLoopStatsSummary> elStats = statisticsService.getAllEventLoopStats();
        assertEquals(2, elStats.size());
        
        // 验证特定EventLoop统计
        EventLoopStats.EventLoopStatsSummary el1Stats = statisticsService.getEventLoopStats("NioEventLoop-1");
        EventLoopStats.EventLoopStatsSummary el2Stats = statisticsService.getEventLoopStats("NioEventLoop-2");
        
        assertNotNull(el1Stats);
        assertNotNull(el2Stats);
        assertEquals(1, el1Stats.getCurrentChannels());
        assertEquals(1, el2Stats.getCurrentChannels());
    }
    
    @Test
    void testStatisticsOverview() {
        // 创建测试数据
        ChannelInfo channelInfo = createTestChannelInfo("test-channel", "TestApp");
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_ACTIVE");
        
        // 获取统计概览
        Map<String, Object> overview = statisticsService.getStatisticsOverview();
        
        assertNotNull(overview);
        assertTrue(overview.containsKey("realTimeStats"));
        assertTrue(overview.containsKey("totalApplications"));
        assertTrue(overview.containsKey("totalEventLoops"));
        
        assertEquals(1, overview.get("totalApplications"));
    }
    
    @Test
    void testResetStatistics() {
        // 创建测试数据
        ChannelInfo channelInfo = createTestChannelInfo("test-channel", "TestApp");
        statisticsService.processChannelEvent(channelInfo, "CHANNEL_ACTIVE");
        
        // 验证有数据
        TimeWindowStats.StatsSummary beforeReset = statisticsService.getRealTimeStats();
        assertTrue(beforeReset.getTotalConnections() > 0);
        
        // 重置统计
        statisticsService.resetAllStats();
        
        // 验证数据被重置
        TimeWindowStats.StatsSummary afterReset = statisticsService.getRealTimeStats();
        assertEquals(0, afterReset.getTotalConnections());
        assertEquals(0, afterReset.getActiveConnections());
        
        List<ApplicationStats.ApplicationStatsSummary> appStats = statisticsService.getAllApplicationStats();
        assertTrue(appStats.isEmpty());
    }
    
    /**
     * 创建测试用的ChannelInfo
     */
    private ChannelInfo createTestChannelInfo(String channelId, String applicationName) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setChannelId(channelId);
        channelInfo.setApplicationName(applicationName);
        channelInfo.setRemoteAddress("127.0.0.1:12345");
        channelInfo.setLocalAddress("127.0.0.1:8080");
        channelInfo.setActive(true);
        channelInfo.setOpen(true);
        channelInfo.setWritable(true);
        channelInfo.setState("ACTIVE");
        channelInfo.setCreateTime(LocalDateTime.now());
        channelInfo.setLastActiveTime(LocalDateTime.now());
        channelInfo.setBytesRead(0);
        channelInfo.setBytesWritten(0);
        channelInfo.setMessagesRead(0);
        channelInfo.setMessagesWritten(0);
        channelInfo.setEventLoopGroup("NioEventLoop");
        channelInfo.setPipeline("decoder -> encoder -> handler");
        channelInfo.setChannelRole("SERVER");
        
        return channelInfo;
    }
}"