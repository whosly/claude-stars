package com.yueny.stars.netty.monitor.agent.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * NoOpMonitor单元测试
 * 
 * @author fengyang
 */
class NoOpMonitorTest {
    
    @Mock
    private ChannelHandlerContext mockCtx;
    
    private NoOpMonitor noOpMonitor;
    private EmbeddedChannel channel;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        channel = new EmbeddedChannel();
        when(mockCtx.channel()).thenReturn(channel);
        
        noOpMonitor = new NoOpMonitor("TestClass", "初始化失败");
    }
    
    @Test
    void testConstructor() {
        String context = "TestContext";
        String reason = "测试原因";
        
        NoOpMonitor monitor = new NoOpMonitor(context, reason);
        
        assertEquals(context, monitor.getContext());
        assertEquals(reason, monitor.getReason());
        assertFalse(monitor.canRecover());
    }
    
    @Test
    void testOnConnect() {
        // 测试连接事件处理 - 应该什么都不做，不抛异常
        assertDoesNotThrow(() -> noOpMonitor.onConnect(mockCtx));
    }
    
    @Test
    void testOnDisconnect() {
        // 测试断开事件处理 - 应该什么都不做，不抛异常
        assertDoesNotThrow(() -> noOpMonitor.onDisconnect(mockCtx));
    }
    
    @Test
    void testOnMessage() {
        // 测试消息事件处理 - 应该什么都不做，不抛异常
        String testMessage = "test message";
        assertDoesNotThrow(() -> noOpMonitor.onMessage(mockCtx, testMessage));
        
        // 测试不同类型的消息
        Integer intMessage = 123;
        assertDoesNotThrow(() -> noOpMonitor.onMessage(mockCtx, intMessage));
        
        // 测试null消息
        assertDoesNotThrow(() -> noOpMonitor.onMessage(mockCtx, null));
    }
    
    @Test
    void testOnException() {
        // 测试异常事件处理 - 应该什么都不做，不抛异常
        RuntimeException testException = new RuntimeException("测试异常");
        assertDoesNotThrow(() -> noOpMonitor.onException(mockCtx, testException));
        
        // 测试不同类型的异常
        IllegalArgumentException argException = new IllegalArgumentException("参数异常");
        assertDoesNotThrow(() -> noOpMonitor.onException(mockCtx, argException));
        
        // 测试null异常
        assertDoesNotThrow(() -> noOpMonitor.onException(mockCtx, null));
    }
    
    @Test
    void testGetStatus() {
        String status = noOpMonitor.getStatus();
        
        assertNotNull(status);
        assertTrue(status.contains("TestClass"));
        assertTrue(status.contains("初始化失败"));
        assertTrue(status.contains("NoOp监控器"));
    }
    
    @Test
    void testCanRecover() {
        // NoOp监控器通常不能自动恢复
        assertFalse(noOpMonitor.canRecover());
    }
    
    @Test
    void testGetReason() {
        assertEquals("初始化失败", noOpMonitor.getReason());
    }
    
    @Test
    void testGetContext() {
        assertEquals("TestClass", noOpMonitor.getContext());
    }
    
    @Test
    void testToString() {
        String toString = noOpMonitor.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("NoOpMonitor"));
        assertTrue(toString.contains("TestClass"));
        assertTrue(toString.contains("初始化失败"));
    }
    
    @Test
    void testMultipleOperations() {
        // 测试连续多次操作都不会出问题
        for (int i = 0; i < 10; i++) {
            final int index = i; // 创建final变量
            assertDoesNotThrow(() -> {
                noOpMonitor.onConnect(mockCtx);
                noOpMonitor.onMessage(mockCtx, "message " + index);
                noOpMonitor.onException(mockCtx, new RuntimeException("exception " + index));
                noOpMonitor.onDisconnect(mockCtx);
            });
        }
        
        // 状态应该保持不变
        assertEquals("TestClass", noOpMonitor.getContext());
        assertEquals("初始化失败", noOpMonitor.getReason());
        assertFalse(noOpMonitor.canRecover());
    }
    
    @Test
    void testDifferentReasons() {
        // 测试不同的降级原因
        String[] reasons = {
            "网络连接失败",
            "配置错误",
            "权限不足",
            "资源不可用",
            "重试次数已达上限"
        };
        
        for (String reason : reasons) {
            NoOpMonitor monitor = new NoOpMonitor("TestClass", reason);
            assertEquals(reason, monitor.getReason());
            assertTrue(monitor.getStatus().contains(reason));
            assertTrue(monitor.toString().contains(reason));
        }
    }
    
    @Test
    void testEmptyContextAndReason() {
        // 测试空的上下文和原因
        NoOpMonitor emptyMonitor = new NoOpMonitor("", "");
        
        assertEquals("", emptyMonitor.getContext());
        assertEquals("", emptyMonitor.getReason());
        assertFalse(emptyMonitor.canRecover());
        
        // 操作应该仍然正常
        assertDoesNotThrow(() -> {
            emptyMonitor.onConnect(mockCtx);
            emptyMonitor.onMessage(mockCtx, "test");
            emptyMonitor.onException(mockCtx, new RuntimeException());
            emptyMonitor.onDisconnect(mockCtx);
        });
    }
    
    @Test
    void testNullContextAndReason() {
        // 测试null的上下文和原因
        NoOpMonitor nullMonitor = new NoOpMonitor(null, null);
        
        assertNull(nullMonitor.getContext());
        assertNull(nullMonitor.getReason());
        assertFalse(nullMonitor.canRecover());
        
        // 操作应该仍然正常
        assertDoesNotThrow(() -> {
            nullMonitor.onConnect(mockCtx);
            nullMonitor.onMessage(mockCtx, "test");
            nullMonitor.onException(mockCtx, new RuntimeException());
            nullMonitor.onDisconnect(mockCtx);
        });
    }
}