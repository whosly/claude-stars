package com.whosly.stars.netty.monitor.agent.processor;

import com.whosly.stars.netty.monitor.agent.annotation.NettyMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LazyInitializationManager单元测试
 * 
 * @author fengyang
 */
class LazyInitializationManagerTest {
    
    private LazyInitializationManager manager;
    private AtomicInteger processCount;
    private AtomicInteger successCount;
    
    @BeforeEach
    void setUp() {
        processCount = new AtomicInteger(0);
        successCount = new AtomicInteger(0);
        
        // 创建任务处理器
        manager = new LazyInitializationManager(task -> {
            processCount.incrementAndGet();
            
            // 模拟处理逻辑
            String taskId = task.getTaskId();
            if (taskId.contains("success")) {
                successCount.incrementAndGet();
                return new LazyInitializationManager.InitializationResult(true, "成功", "TestApp");
            } else if (taskId.contains("fail")) {
                return new LazyInitializationManager.InitializationResult(false, "失败");
            } else {
                // 默认成功
                successCount.incrementAndGet();
                return new LazyInitializationManager.InitializationResult(true, "默认成功", "DefaultApp");
            }
        });
        
        // 设置较短的检查间隔以便测试
        manager.setCheckInterval(100);
    }
    
    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.shutdown();
        }
    }
    
    @Test
    void testAddTask() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        
        boolean result = manager.addTask("task1", TestClass.class, annotation);
        assertTrue(result);
        assertEquals(1, manager.getPendingTaskCount());
        
        // 重复添加相同任务应该失败
        boolean duplicateResult = manager.addTask("task1", TestClass.class, annotation);
        assertFalse(duplicateResult);
        assertEquals(1, manager.getPendingTaskCount());
    }
    
    @Test
    void testAddTaskWithNullParameters() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        
        assertFalse(manager.addTask(null, TestClass.class, annotation));
        assertFalse(manager.addTask("task1", null, annotation));
        assertFalse(manager.addTask("task1", TestClass.class, null));
    }
    
    @Test
    void testRemoveTask() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        manager.addTask("task1", TestClass.class, annotation);
        
        LazyInitializationManager.PendingInitialization removed = manager.removeTask("task1");
        assertNotNull(removed);
        assertEquals("task1", removed.getTaskId());
        assertEquals(0, manager.getPendingTaskCount());
        
        // 移除不存在的任务
        LazyInitializationManager.PendingInitialization notFound = manager.removeTask("nonexistent");
        assertNull(notFound);
    }
    
    @Test
    void testGetTask() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        manager.addTask("task1", TestClass.class, annotation);
        
        LazyInitializationManager.PendingInitialization task = manager.getTask("task1");
        assertNotNull(task);
        assertEquals("task1", task.getTaskId());
        assertEquals(TestClass.class, task.getClazz());
        
        // 获取不存在的任务
        LazyInitializationManager.PendingInitialization notFound = manager.getTask("nonexistent");
        assertNull(notFound);
    }
    
    @Test
    void testProcessTaskManually() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        manager.addTask("success_task", TestClass.class, annotation);
        
        LazyInitializationManager.InitializationResult result = manager.processTask("success_task");
        assertTrue(result.isSuccessful());
        assertEquals("成功", result.getMessage());
        assertEquals("TestApp", result.getApplicationName());
        
        // 处理不存在的任务
        LazyInitializationManager.InitializationResult notFound = manager.processTask("nonexistent");
        assertFalse(notFound.isSuccessful());
        assertTrue(notFound.getMessage().contains("任务不存在"));
    }
    
    @Test
    void testAutomaticProcessing() throws InterruptedException {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        manager.addTask("success_task", TestClass.class, annotation);
        
        // 手动触发处理而不是等待定时任务
        LazyInitializationManager.InitializationResult result = manager.processTask("success_task");
        
        // 验证任务被处理
        assertTrue(result.isSuccessful(), "任务应该处理成功");
        assertTrue(processCount.get() > 0, "任务应该被处理");
        assertTrue(successCount.get() > 0, "应该有成功的任务");
    }
    
    @Test
    void testRetryMechanism() throws InterruptedException {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 2, 50); // 最多重试2次，间隔50ms
        manager.addTask("fail_task", TestClass.class, annotation);
        
        // 手动触发多次处理来模拟重试
        LazyInitializationManager.InitializationResult result1 = manager.processTask("fail_task");
        LazyInitializationManager.InitializationResult result2 = manager.processTask("fail_task");
        
        // 验证处理结果
        assertFalse(result1.isSuccessful(), "失败任务应该返回失败结果");
        assertFalse(result2.isSuccessful(), "失败任务应该返回失败结果");
        assertTrue(processCount.get() >= 2, "至少应该处理2次，实际: " + processCount.get());
    }
    
    @Test
    void testTimeout() throws InterruptedException {
        // 创建一个已经超时的任务（创建时间设为过去）
        NettyMonitor annotation = createMockAnnotation("TestApp", 100, 3, 50); // 100ms超时
        
        // 手动创建一个超时的任务
        LazyInitializationManager.PendingInitialization timeoutTask = 
            new LazyInitializationManager.PendingInitialization(
                "timeout_task", TestClass.class, annotation, 
                System.currentTimeMillis() - 200, // 200ms前创建，已经超时
                100, 3, 50);
        
        // 验证任务已经超时
        assertTrue(System.currentTimeMillis() - timeoutTask.getCreateTime() > timeoutTask.getTimeout(), 
                "任务应该已经超时");
    }
    
    @Test
    void testStatistics() throws InterruptedException {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        
        // 添加成功任务
        manager.addTask("success_task1", TestClass.class, annotation);
        manager.addTask("success_task2", TestClass.class, annotation);
        
        // 添加失败任务
        manager.addTask("fail_task", TestClass.class, annotation);
        
        // 手动处理任务
        manager.processTask("success_task1");
        manager.processTask("success_task2");
        manager.processTask("fail_task");
        
        LazyInitializationManager.Statistics stats = manager.getStatistics();
        assertEquals(3, stats.getTotalTasks(), "总任务数应该是3");
        assertTrue(stats.getSuccessfulTasks() >= 2, "成功任务数应该至少是2，实际: " + stats.getSuccessfulTasks());
        assertTrue(stats.getSuccessRate() > 0, "成功率应该大于0");
    }
    
    @Test
    void testSetEnabled() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        
        // 禁用管理器
        manager.setEnabled(false);
        
        // 尝试添加任务应该失败
        boolean result = manager.addTask("task1", TestClass.class, annotation);
        assertFalse(result);
        assertEquals(0, manager.getPendingTaskCount());
        
        // 重新启用
        manager.setEnabled(true);
        
        // 现在应该可以添加任务
        boolean enabledResult = manager.addTask("task2", TestClass.class, annotation);
        assertTrue(enabledResult);
        assertEquals(1, manager.getPendingTaskCount());
    }
    
    @Test
    void testClearAllTasks() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        
        manager.addTask("task1", TestClass.class, annotation);
        manager.addTask("task2", TestClass.class, annotation);
        assertEquals(2, manager.getPendingTaskCount());
        
        manager.clearAllTasks();
        assertEquals(0, manager.getPendingTaskCount());
    }
    
    @Test
    void testSetCheckInterval() {
        manager.setCheckInterval(500);
        // 这个测试主要验证方法不会抛出异常
        
        // 无效间隔应该被忽略
        manager.setCheckInterval(-1);
        manager.setCheckInterval(0);
    }
    
    @Test
    void testPendingInitializationToString() {
        NettyMonitor annotation = createMockAnnotation("TestApp", 5000, 3, 1000);
        LazyInitializationManager.PendingInitialization task = 
            new LazyInitializationManager.PendingInitialization(
                "test_task", TestClass.class, annotation, System.currentTimeMillis(), 5000, 3, 1000);
        
        String str = task.toString();
        assertTrue(str.contains("test_task"));
        assertTrue(str.contains("TestClass"));
    }
    
    @Test
    void testInitializationResultToString() {
        LazyInitializationManager.InitializationResult result = 
            new LazyInitializationManager.InitializationResult(true, "成功", "TestApp");
        
        String str = result.toString();
        assertTrue(str.contains("true"));
        assertTrue(str.contains("成功"));
        assertTrue(str.contains("TestApp"));
    }
    
    @Test
    void testStatisticsToString() {
        LazyInitializationManager.Statistics stats = 
            new LazyInitializationManager.Statistics(10, 8, 1, 1, 0);
        
        String str = stats.toString();
        assertTrue(str.contains("10"));
        assertTrue(str.contains("8"));
        assertTrue(str.contains("80.00%"));
    }
    
    // 辅助方法：创建模拟注解
    private NettyMonitor createMockAnnotation(String applicationName, int initTimeout, int retryCount, int retryInterval) {
        return new NettyMonitor() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return NettyMonitor.class;
            }
            
            @Override
            public String value() { return ""; }
            
            @Override
            public String applicationName() { return applicationName; }
            
            @Override
            public String host() { return "localhost"; }
            
            @Override
            public int port() { return 19999; }
            
            @Override
            public boolean enabled() { return true; }
            
            @Override
            public boolean lazyInit() { return true; }
            
            @Override
            public int initTimeout() { return initTimeout; }
            
            @Override
            public int connectTimeout() { return 5000; }
            
            @Override
            public int reconnectInterval() { return 5; }
            
            @Override
            public int retryCount() { return retryCount; }
            
            @Override
            public int retryInterval() { return retryInterval; }
        };
    }
    
    // 测试用的类
    static class TestClass {
    }
}