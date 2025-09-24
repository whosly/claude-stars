package com.whosly.stars.netty.monitor.agent.template.resolver;

import com.whosly.stars.netty.monitor.agent.context.MonitorContextManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContextVariableResolver单元测试
 * 
 * @author fengyang
 */
class ContextVariableResolverTest {
    
    private ContextVariableResolver resolver;
    
    @BeforeEach
    void setUp() {
        resolver = new ContextVariableResolver();
        // 清空上下文，确保测试环境干净
        MonitorContextManager.clearGlobalContext();
        MonitorContextManager.clearThreadContext();
    }
    
    @AfterEach
    void tearDown() {
        // 清空上下文
        MonitorContextManager.clearGlobalContext();
        MonitorContextManager.clearThreadContext();
    }
    
    @Test
    void testCanResolveWithGlobalContext() {
        // 设置全局上下文变量
        MonitorContextManager.setGlobalContext("username", "Alice");
        
        // 测试能够解析存在的变量
        assertTrue(resolver.canResolve("username"));
        assertTrue(resolver.canResolve("username:default"));
        
        // 测试不能解析不存在的变量
        assertFalse(resolver.canResolve("nonexistent"));
    }
    
    @Test
    void testCanResolveWithThreadContext() {
        // 设置线程本地上下文变量
        MonitorContextManager.setThreadContext("clientId", "client123");
        
        // 测试能够解析存在的变量
        assertTrue(resolver.canResolve("clientId"));
        assertTrue(resolver.canResolve("clientId:default"));
        
        // 测试不能解析不存在的变量
        assertFalse(resolver.canResolve("nonexistent"));
    }
    
    @Test
    void testCanResolveEdgeCases() {
        // 测试边界情况
        assertFalse(resolver.canResolve(null));
        assertFalse(resolver.canResolve(""));
        assertFalse(resolver.canResolve("   "));
    }
    
    @Test
    void testResolveFromGlobalContext() {
        // 设置全局上下文变量
        MonitorContextManager.setGlobalContext("appName", "TestApp");
        
        // 测试解析全局上下文变量
        Object result = resolver.resolve("appName", null);
        assertEquals("TestApp", result.toString());
    }
    
    @Test
    void testResolveFromThreadContext() {
        // 设置线程本地上下文变量
        MonitorContextManager.setThreadContext("sessionId", "session456");
        
        // 测试解析线程本地上下文变量
        Object result = resolver.resolve("sessionId", null);
        assertEquals("session456", result.toString());
    }
    
    @Test
    void testThreadContextPriority() {
        // 设置相同键的全局和线程本地上下文变量
        MonitorContextManager.setGlobalContext("priority", "global");
        MonitorContextManager.setThreadContext("priority", "thread");
        
        // 线程本地上下文应该有更高优先级
        Object result = resolver.resolve("priority", null);
        assertEquals("thread", result.toString());
    }
    
    @Test
    void testResolveWithDefaultValue() {
        // 测试解析不存在的变量，使用默认值
        Object result = resolver.resolve("nonexistent:defaultValue", null);
        assertEquals("defaultValue", result.toString());
        
        // 测试存在的变量，不使用默认值
        MonitorContextManager.setGlobalContext("existing", "actualValue");
        result = resolver.resolve("existing:defaultValue", null);
        assertEquals("actualValue", result.toString());
    }
    
    @Test
    void testResolveWithEmptyDefaultValue() {
        // 测试空默认值
        Object result = resolver.resolve("nonexistent:", null);
        assertEquals("", result.toString());
    }
    
    @Test
    void testResolveWithColonInDefaultValue() {
        // 测试默认值中包含冒号的情况
        Object result = resolver.resolve("nonexistent:http://localhost:8080", null);
        assertEquals("http://localhost:8080", result.toString());
    }
    
    @Test
    void testResolveNonExistentVariable() {
        // 测试解析不存在的变量且无默认值
        Object result = resolver.resolve("nonexistent", null);
        assertNull(result);
    }
    
    @Test
    void testResolveNullVariable() {
        // 测试null变量
        Object result = resolver.resolve(null, null);
        assertNull(result);
    }
    
    @Test
    void testResolveEmptyVariable() {
        // 测试空变量
        Object result = resolver.resolve("", null);
        assertNull(result);
        
        result = resolver.resolve("   ", null);
        assertNull(result);
    }
    
    @Test
    void testGetPriority() {
        // 测试优先级（应该是高优先级：10）
        assertEquals(10, resolver.getPriority());
    }
    
    @Test
    void testGetName() {
        // 测试解析器名称
        assertEquals("ContextVariableResolver", resolver.getName());
    }
    
    @Test
    void testResolveWithWhitespace() {
        // 设置上下文变量
        MonitorContextManager.setGlobalContext("test", "value");
        
        // 测试变量名和默认值包含空格的情况
        Object result = resolver.resolve(" test : default ", null);
        assertEquals("value", result.toString());
        
        // 测试不存在的变量
        result = resolver.resolve(" nonexistent : default ", null);
        assertEquals("default", result.toString());
    }
    
    @Test
    void testResolveWithDifferentValueTypes() {
        // 测试不同类型的值
        MonitorContextManager.setGlobalContext("intValue", 123);
        MonitorContextManager.setGlobalContext("boolValue", true);
        MonitorContextManager.setGlobalContext("doubleValue", 3.14);
        
        // 测试整数值
        Object result = resolver.resolve("intValue", null);
        assertEquals(123, result);
        
        // 测试布尔值
        result = resolver.resolve("boolValue", null);
        assertEquals(true, result);
        
        // 测试浮点数值
        result = resolver.resolve("doubleValue", null);
        assertEquals(3.14, result);
    }
    
    @Test
    void testMultipleThreadsIndependence() throws InterruptedException {
        // 测试多线程环境下的独立性
        final String[] results = new String[2];
        final Exception[] exceptions = new Exception[2];
        
        Thread thread1 = new Thread(() -> {
            try {
                MonitorContextManager.setThreadContext("threadVar", "thread1Value");
                results[0] = resolver.resolve("threadVar", null).toString();
            } catch (Exception e) {
                exceptions[0] = e;
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                MonitorContextManager.setThreadContext("threadVar", "thread2Value");
                results[1] = resolver.resolve("threadVar", null).toString();
            } catch (Exception e) {
                exceptions[1] = e;
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // 检查没有异常
        assertNull(exceptions[0]);
        assertNull(exceptions[1]);
        
        // 检查每个线程获取到自己的值
        assertEquals("thread1Value", results[0]);
        assertEquals("thread2Value", results[1]);
    }
}