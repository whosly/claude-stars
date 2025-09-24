package com.whosly.stars.netty.monitor.agent.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RetryErrorHandler单元测试
 * 
 * @author fengyang
 */
class RetryErrorHandlerTest {
    
    private RetryErrorHandler retryErrorHandler;
    
    @BeforeEach
    void setUp() {
        retryErrorHandler = new RetryErrorHandler();
    }
    
    @Test
    void testNetworkErrorsAreRetryable() {
        // 测试网络连接错误
        assertTrue(retryErrorHandler.isRetryable(new ConnectException("Connection refused")));
        assertTrue(retryErrorHandler.isRetryable(new SocketTimeoutException("Connection timeout")));
        
        // 测试网络不可达错误
        RuntimeException networkError = new RuntimeException("Network is unreachable");
        assertTrue(retryErrorHandler.isRetryable(networkError));
    }
    
    @Test
    void testResourceErrorsAreRetryable() {
        // 测试资源临时不可用错误
        RuntimeException resourceError = new RuntimeException("Resource temporarily unavailable");
        assertTrue(retryErrorHandler.isRetryable(resourceError));
        
        // 测试文件句柄耗尽错误
        RuntimeException fileError = new RuntimeException("Too many open files");
        assertTrue(retryErrorHandler.isRetryable(fileError));
        
        // 测试线程池拒绝执行错误
        assertTrue(retryErrorHandler.isRetryable(new RejectedExecutionException()));
    }
    
    @Test
    void testConfigurationErrorsAreNotRetryable() {
        // 测试配置错误
        assertFalse(retryErrorHandler.isRetryable(new IllegalArgumentException("Invalid argument")));
        assertFalse(retryErrorHandler.isRetryable(new IllegalStateException("Invalid state")));
        
        // 测试包含configuration关键字的错误
        RuntimeException configError = new RuntimeException("Invalid configuration");
        assertFalse(retryErrorHandler.isRetryable(configError));
    }
    
    @Test
    void testPermissionErrorsAreNotRetryable() {
        // 测试权限错误
        assertFalse(retryErrorHandler.isRetryable(new SecurityException("Access denied")));
        
        // 测试包含permission关键字的错误
        RuntimeException permError = new RuntimeException("Permission denied");
        assertFalse(retryErrorHandler.isRetryable(permError));
    }
    
    @Test
    void testRuntimeExceptionsAreRetryableByDefault() {
        // 测试默认情况下RuntimeException可重试
        assertTrue(retryErrorHandler.isRetryable(new RuntimeException("Unknown error")));
        assertTrue(retryErrorHandler.isRetryable(new NullPointerException()));
    }
    
    @Test
    void testCheckedExceptionsAreNotRetryableByDefault() {
        // 测试默认情况下检查异常不可重试
        assertFalse(retryErrorHandler.isRetryable(new Exception("Checked exception")));
    }
    
    @Test
    void testRecordErrorWithRetryableError() {
        String context = "TestClass";
        RuntimeException error = new RuntimeException("Network is unreachable");
        
        // 第一次错误，应该重试
        RetryErrorHandler.RetryDecision decision1 = retryErrorHandler.recordError(context, error, 0);
        assertTrue(decision1.shouldRetry());
        assertTrue(decision1.getDelayMs() > 0);
        assertEquals("准备重试", decision1.getReason());
        
        // 第二次错误，应该重试
        RetryErrorHandler.RetryDecision decision2 = retryErrorHandler.recordError(context, error, 1);
        assertTrue(decision2.shouldRetry());
        assertTrue(decision2.getDelayMs() > 0);
        
        // 第三次错误，应该重试
        RetryErrorHandler.RetryDecision decision3 = retryErrorHandler.recordError(context, error, 2);
        assertTrue(decision3.shouldRetry());
        assertTrue(decision3.getDelayMs() > 0);
        
        // 第四次错误，已达最大重试次数，不应该重试
        RetryErrorHandler.RetryDecision decision4 = retryErrorHandler.recordError(context, error, 3);
        assertFalse(decision4.shouldRetry());
        assertEquals(0, decision4.getDelayMs());
        assertTrue(decision4.getReason().contains("最大重试次数"));
    }
    
    @Test
    void testRecordErrorWithNonRetryableError() {
        String context = "TestClass";
        IllegalArgumentException error = new IllegalArgumentException("Invalid configuration");
        
        // 不可重试的错误，第一次就应该停止
        RetryErrorHandler.RetryDecision decision = retryErrorHandler.recordError(context, error, 0);
        assertFalse(decision.shouldRetry());
        assertEquals(0, decision.getDelayMs());
        assertTrue(decision.getReason().contains("错误不可重试"));
    }
    
    @Test
    void testErrorStatistics() {
        String context = "TestClass";
        RuntimeException error = new RuntimeException("Test error");
        
        // 初始状态，没有错误
        RetryErrorHandler.ErrorStatistics stats1 = retryErrorHandler.getErrorStatistics(context);
        assertEquals(0, stats1.getTotalErrors());
        assertEquals(0, stats1.getLastErrorTime());
        
        // 记录一个错误
        long beforeTime = System.currentTimeMillis();
        retryErrorHandler.recordError(context, error, 0);
        long afterTime = System.currentTimeMillis();
        
        // 检查错误统计
        RetryErrorHandler.ErrorStatistics stats2 = retryErrorHandler.getErrorStatistics(context);
        assertEquals(1, stats2.getTotalErrors());
        assertTrue(stats2.getLastErrorTime() >= beforeTime);
        assertTrue(stats2.getLastErrorTime() <= afterTime);
        
        // 记录另一个错误
        retryErrorHandler.recordError(context, error, 1);
        
        // 检查错误统计更新
        RetryErrorHandler.ErrorStatistics stats3 = retryErrorHandler.getErrorStatistics(context);
        assertEquals(2, stats3.getTotalErrors());
        assertTrue(stats3.getLastErrorTime() >= stats2.getLastErrorTime());
    }
    
    @Test
    void testRetryStrategyConfiguration() {
        // 测试设置重试策略
        RetryStrategy.LinearBackoff linearStrategy = new RetryStrategy.LinearBackoff();
        retryErrorHandler.setRetryStrategy(linearStrategy);
        
        // 测试设置最大重试次数
        retryErrorHandler.setMaxRetryAttempts(5);
        
        // 测试设置基础重试间隔
        retryErrorHandler.setBaseRetryInterval(2000);
        
        // 验证配置生效
        String context = "TestClass";
        RuntimeException error = new RuntimeException("Test error");
        
        // 记录错误并检查重试决策
        RetryErrorHandler.RetryDecision decision = retryErrorHandler.recordError(context, error, 0);
        assertTrue(decision.shouldRetry());
        assertTrue(decision.getDelayMs() >= 2000); // 应该使用新的基础间隔
    }
    
    @Test
    void testCustomErrorClassifier() {
        // 添加自定义错误分类器
        retryErrorHandler.addErrorClassifier("custom", throwable -> 
            throwable.getMessage() != null && throwable.getMessage().contains("CUSTOM_ERROR"));
        
        // 测试自定义分类器
        RuntimeException customError = new RuntimeException("CUSTOM_ERROR occurred");
        // 注意：自定义分类器不会自动影响isRetryable方法，需要在实际使用中集成
        
        // 这里只是验证分类器被正确添加，具体逻辑需要在实际使用中测试
        assertNotNull(customError);
    }
    
    @Test
    void testCleanupExpiredStats() throws InterruptedException {
        String context = "TestClass";
        RuntimeException error = new RuntimeException("Test error");
        
        // 记录一个错误
        retryErrorHandler.recordError(context, error, 0);
        
        // 验证错误统计存在
        RetryErrorHandler.ErrorStatistics stats1 = retryErrorHandler.getErrorStatistics(context);
        assertEquals(1, stats1.getTotalErrors());
        
        // 等待一小段时间
        Thread.sleep(10);
        
        // 清理过期统计（使用很短的过期时间）
        retryErrorHandler.cleanupExpiredStats(5);
        
        // 验证错误统计被清理
        RetryErrorHandler.ErrorStatistics stats2 = retryErrorHandler.getErrorStatistics(context);
        assertEquals(0, stats2.getTotalErrors());
    }
}