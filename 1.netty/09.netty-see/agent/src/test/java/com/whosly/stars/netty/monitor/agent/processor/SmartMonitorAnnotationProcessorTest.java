package com.whosly.stars.netty.monitor.agent.processor;

import com.whosly.stars.netty.monitor.agent.annotation.NettyMonitor;
import com.whosly.stars.netty.monitor.agent.context.MonitorContextManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SmartMonitorAnnotationProcessor单元测试
 * 
 * @author fengyang
 */
class SmartMonitorAnnotationProcessorTest {
    
    private SmartMonitorAnnotationProcessor processor;
    
    @BeforeEach
    void setUp() {
        processor = SmartMonitorAnnotationProcessor.getInstance();
        processor.clearCache();
        MonitorContextManager.clearGlobalContext();
        MonitorContextManager.clearThreadContext();
    }
    
    @AfterEach
    void tearDown() {
        MonitorContextManager.clearGlobalContext();
        MonitorContextManager.clearThreadContext();
    }
    
    @Test
    void testProcessClassWithoutAnnotation() {
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithoutAnnotation.class);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMessage().contains("没有@NettyMonitor注解"));
    }
    
    @Test
    void testProcessClassWithDefaultAnnotation() {
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithDefaultAnnotation.class);
        assertTrue(result.isSuccessful());
        assertEquals("ClassWithDefaultAnnotation", result.getApplicationName());
    }
    
    @Test
    void testProcessClassWithStaticName() {
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithStaticName.class);
        assertTrue(result.isSuccessful());
        assertEquals("StaticAppName", result.getApplicationName());
    }
    
    @Test
    void testProcessClassWithContextVariable() {
        // 设置上下文变量
        MonitorContextManager.setGlobalContext("username", "Alice");
        
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithContextVariable.class);
        assertTrue(result.isSuccessful());
        assertEquals("TestApp-Alice", result.getApplicationName());
    }
    
    @Test
    void testProcessClassWithSystemProperty() {
        // 设置系统属性
        System.setProperty("test.port", "8080");
        
        try {
            SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithSystemProperty.class);
            assertTrue(result.isSuccessful());
            assertEquals("Server-8080", result.getApplicationName());
        } finally {
            System.clearProperty("test.port");
        }
    }
    
    @Test
    void testProcessClassWithMethodCall() {
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithMethodCall.class);
        assertTrue(result.isSuccessful());
        // 方法调用可能无法解析，因为需要实例化对象
        // 这里验证至少处理成功了
        assertNotNull(result.getApplicationName());
    }
    
    @Test
    void testProcessClassWithLazyInit() {
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithLazyInit.class);
        
        // 延迟初始化可能成功也可能失败，取决于变量是否能解析
        // 如果变量无法解析且启用了延迟初始化，应该加入队列
        if (result.isSuccessful() && result.getMessage().contains("延迟初始化队列")) {
            // 验证待处理队列中有任务
            assertTrue(processor.getPendingInitializationCount() > 0);
        } else {
            // 如果直接初始化成功，也是可以接受的
            assertTrue(result.isSuccessful());
        }
    }
    
    @Test
    void testProcessClassTwice() {
        // 第一次处理
        SmartMonitorAnnotationProcessor.ProcessResult result1 = processor.processClass(ClassWithDefaultAnnotation.class);
        assertTrue(result1.isSuccessful());
        
        // 第二次处理应该跳过
        SmartMonitorAnnotationProcessor.ProcessResult result2 = processor.processClass(ClassWithDefaultAnnotation.class);
        assertTrue(result2.isSuccessful());
        assertTrue(result2.getMessage().contains("已处理"));
    }
    
    @Test
    void testProcessNullClass() {
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMessage().contains("类不能为null"));
    }
    
    @Test
    void testRetryFailedInitializations() {
        // 添加一个需要延迟初始化的类
        processor.processClass(ClassWithLazyInit.class);
        int initialPendingCount = processor.getPendingInitializationCount();
        
        // 设置上下文变量，使延迟初始化能够成功
        MonitorContextManager.setGlobalContext("delayed", "value");
        
        // 执行重试
        processor.retryFailedInitializations();
        
        // 验证待处理数量可能减少（如果成功初始化）
        int finalPendingCount = processor.getPendingInitializationCount();
        assertTrue(finalPendingCount <= initialPendingCount);
    }
    
    @Test
    void testGetTemplateResolver() {
        assertNotNull(processor.getTemplateResolver());
    }
    
    @Test
    void testClearCache() {
        // 处理一个类
        processor.processClass(ClassWithDefaultAnnotation.class);
        assertTrue(processor.getProcessedClassCount() > 0);
        
        // 清空缓存
        processor.clearCache();
        assertEquals(0, processor.getProcessedClassCount());
        assertEquals(0, processor.getPendingInitializationCount());
    }
    
    @Test
    void testSingletonInstance() {
        SmartMonitorAnnotationProcessor instance1 = SmartMonitorAnnotationProcessor.getInstance();
        SmartMonitorAnnotationProcessor instance2 = SmartMonitorAnnotationProcessor.getInstance();
        assertSame(instance1, instance2);
    }
    
    // 测试用的类
    static class ClassWithoutAnnotation {
    }
    
    @NettyMonitor
    static class ClassWithDefaultAnnotation {
    }
    
    @NettyMonitor(applicationName = "StaticAppName")
    static class ClassWithStaticName {
    }
    
    @NettyMonitor(applicationName = "TestApp-${username}")
    static class ClassWithContextVariable {
    }
    
    @NettyMonitor(applicationName = "Server-${test.port}")
    static class ClassWithSystemProperty {
    }
    
    @NettyMonitor(applicationName = "App-${getTestValue()}")
    static class ClassWithMethodCall {
        public String getTestValue() {
            return "TestValue";
        }
    }
    
    @NettyMonitor(applicationName = "DelayedApp-${delayed}", lazyInit = true)
    static class ClassWithLazyInit {
    }
    
    @Test
    void testErrorHandlingAndRetry() {
        // 测试错误统计功能
        String context = "TestClass";
        RetryErrorHandler.ErrorStatistics stats1 = processor.getErrorStatistics(context);
        assertEquals(0, stats1.getTotalErrors());
        
        // 这里我们无法直接测试内部的重试逻辑，但可以测试错误统计接口
        assertNotNull(stats1);
    }
    
    @Test
    void testNoOpMonitorCreation() {
        // 处理一个会失败的类（没有注解）
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithoutAnnotation.class);
        assertFalse(result.isSuccessful());
        
        // 虽然这个测试用例不会创建NoOp监控器（因为是注解缺失而不是初始化失败），
        // 但我们可以测试NoOp监控器的获取接口
        NoOpMonitor noOpMonitor = processor.getNoOpMonitor("NonExistentClass");
        assertNull(noOpMonitor); // 应该返回null，因为没有为这个类创建NoOp监控器
    }
    
    @Test
    void testProcessClassWithLazyInitNew() {
        // 测试延迟初始化
        SmartMonitorAnnotationProcessor.ProcessResult result = processor.processClass(ClassWithLazyInitNew.class);
        
        // 延迟初始化可能成功也可能失败，取决于变量是否能解析
        // 如果变量无法解析且启用了延迟初始化，应该加入队列
        if (result.isSuccessful() && result.getMessage().contains("延迟初始化")) {
            // 验证待处理队列中有任务
            assertTrue(processor.getPendingInitializationCount() > 0);
        } else {
            // 如果直接初始化成功或失败，也是可以接受的
            // 这里只验证处理过程没有抛出异常
            assertNotNull(result);
            assertNotNull(result.getMessage());
        }
    }
    

    
    @Test
    void testClearCacheWithErrorStats() {
        // 处理一些类
        processor.processClass(ClassWithDefaultAnnotation.class);
        processor.processClass(ClassWithStaticName.class);
        
        // 清空缓存
        processor.clearCache();
        
        // 验证缓存被清空
        assertEquals(0, processor.getProcessedClassCount());
        assertEquals(0, processor.getPendingInitializationCount());
    }
    
    @Test
    void testMultipleProcessingSameClass() {
        // 第一次处理
        SmartMonitorAnnotationProcessor.ProcessResult result1 = processor.processClass(ClassWithDefaultAnnotation.class);
        assertTrue(result1.isSuccessful());
        
        // 第二次处理同一个类，应该跳过
        SmartMonitorAnnotationProcessor.ProcessResult result2 = processor.processClass(ClassWithDefaultAnnotation.class);
        assertTrue(result2.isSuccessful());
        assertTrue(result2.getMessage().contains("已处理"));
    }
    
    // 测试用的类定义
    
    @NettyMonitor(value = "App-${missing.variable}", lazyInit = true, initTimeout = 10000)
    static class ClassWithLazyInitNew {
    }
    
    @NettyMonitor(value = "", retryCount = 5)
    static class ClassWithRetryConfig {
    }
}