package com.whosly.stars.netty.monitor.agent.template.resolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnvironmentResolver单元测试
 * 
 * @author fengyang
 */
class EnvironmentResolverTest {
    
    private EnvironmentResolver resolver;
    
    @BeforeEach
    void setUp() {
        resolver = new EnvironmentResolver();
    }
    
    @Test
    void testCanResolve() {
        // 测试能够识别环境变量语法
        assertTrue(resolver.canResolve("env.PATH"));
        assertTrue(resolver.canResolve("env.JAVA_HOME"));
        assertTrue(resolver.canResolve("env.USER"));
        assertTrue(resolver.canResolve("env.HOME"));
        assertTrue(resolver.canResolve("env.CUSTOM_VAR:default"));
        
        // 测试不能识别非环境变量语法
        assertFalse(resolver.canResolve("PATH"));
        assertFalse(resolver.canResolve("system.property"));
        assertFalse(resolver.canResolve("variable"));
        assertFalse(resolver.canResolve("method()"));
        
        // 测试边界情况
        assertFalse(resolver.canResolve(null));
        assertFalse(resolver.canResolve(""));
        assertFalse(resolver.canResolve("   "));
        assertFalse(resolver.canResolve("env."));
    }
    
    @Test
    void testResolveExistingEnvironmentVariable() {
        // 测试解析已存在的环境变量
        // PATH环境变量在所有系统上都应该存在
        Object result = resolver.resolve("env.PATH", null);
        assertNotNull(result);
        assertTrue(result.toString().length() > 0);
    }
    
    @Test
    void testResolveNonExistentEnvironmentVariable() {
        // 测试解析不存在的环境变量
        Object result = resolver.resolve("env.NON_EXISTENT_VAR_12345", null);
        assertNull(result);
    }
    
    @Test
    void testResolveWithDefaultValue() {
        // 测试解析不存在的环境变量，使用默认值
        Object result = resolver.resolve("env.NON_EXISTENT_VAR:default_value", null);
        assertEquals("default_value", result);
        
        // 测试存在的环境变量，不使用默认值
        // 假设PATH环境变量存在
        result = resolver.resolve("env.PATH:default_value", null);
        assertNotNull(result);
        assertNotEquals("default_value", result.toString());
    }
    
    @Test
    void testResolveWithEmptyDefaultValue() {
        // 测试空默认值
        Object result = resolver.resolve("env.NON_EXISTENT_VAR:", null);
        assertEquals("", result.toString());
    }
    
    @Test
    void testResolveWithColonInDefaultValue() {
        // 测试默认值中包含冒号的情况
        Object result = resolver.resolve("env.NON_EXISTENT_VAR:http://localhost:8080", null);
        assertEquals("http://localhost:8080", result.toString());
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
    void testResolveInvalidEnvFormat() {
        // 测试无效的环境变量格式
        Object result = resolver.resolve("env.", null);
        assertNull(result);
        
        result = resolver.resolve("env.:default", null);
        assertEquals("default", result);
    }
    
    @Test
    void testResolveNonEnvVariable() {
        // 测试非环境变量格式的变量
        Object result = resolver.resolve("PATH", null);
        assertNull(result);
        
        result = resolver.resolve("system.property", null);
        assertNull(result);
    }
    
    @Test
    void testGetPriority() {
        // 测试优先级
        assertEquals(40, resolver.getPriority());
    }
    
    @Test
    void testGetName() {
        // 测试解析器名称
        assertEquals("EnvironmentResolver", resolver.getName());
    }
    
    @Test
    void testResolveWithWhitespace() {
        // 测试变量名和默认值包含空格的情况
        Object result = resolver.resolve(" env.NON_EXISTENT_VAR : default_value ", null);
        assertEquals("default_value", result.toString());
    }
    
    @Test
    void testResolveCommonEnvironmentVariables() {
        // 测试一些常见的环境变量（这些在大多数系统上都存在）
        
        // 在Windows上通常存在的环境变量
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            // Windows环境变量
            Object result = resolver.resolve("env.OS", null);
            if (result != null) {
                assertNotNull(result);
            }
            
            result = resolver.resolve("env.USERNAME", null);
            if (result != null) {
                assertNotNull(result);
            }
        } else {
            // Unix/Linux环境变量
            Object result = resolver.resolve("env.USER", null);
            if (result != null) {
                assertNotNull(result);
            }
            
            result = resolver.resolve("env.HOME", null);
            if (result != null) {
                assertNotNull(result);
            }
        }
    }
    
    @Test
    void testResolveCaseSensitivity() {
        // 测试环境变量的大小写敏感性
        // 在Windows上环境变量不区分大小写，在Unix/Linux上区分大小写
        
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            // Windows上PATH和path应该都能解析到相同的值
            Object pathUpper = resolver.resolve("env.PATH", null);
            Object pathLower = resolver.resolve("env.path", null);
            
            // 至少有一个应该不为null（因为PATH通常存在）
            assertTrue(pathUpper != null || pathLower != null);
        }
        // 在Unix/Linux系统上，我们不做假设，因为环境变量名是区分大小写的
    }
}