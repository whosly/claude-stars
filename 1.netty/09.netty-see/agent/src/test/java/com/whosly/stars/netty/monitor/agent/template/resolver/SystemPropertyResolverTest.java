package com.whosly.stars.netty.monitor.agent.template.resolver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SystemPropertyResolver单元测试
 * 
 * @author fengyang
 */
class SystemPropertyResolverTest {
    
    private SystemPropertyResolver resolver;
    private String originalProperty;
    
    @BeforeEach
    void setUp() {
        resolver = new SystemPropertyResolver();
        // 保存原始属性值，以便测试后恢复
        originalProperty = System.getProperty("test.property");
    }
    
    @AfterEach
    void tearDown() {
        // 恢复原始属性值
        if (originalProperty != null) {
            System.setProperty("test.property", originalProperty);
        } else {
            System.clearProperty("test.property");
        }
    }
    
    @Test
    void testCanResolve() {
        // 测试正常变量名
        assertTrue(resolver.canResolve("java.version"));
        assertTrue(resolver.canResolve("user.name"));
        assertTrue(resolver.canResolve("test.property"));
        
        // 测试带默认值的变量
        assertTrue(resolver.canResolve("test.property:default"));
        
        // 测试边界情况
        assertFalse(resolver.canResolve(null));
        assertFalse(resolver.canResolve(""));
        assertFalse(resolver.canResolve("   "));
    }
    
    @Test
    void testResolveExistingProperty() {
        // 测试解析已存在的系统属性
        Object result = resolver.resolve("java.version", null);
        assertNotNull(result);
        assertEquals(System.getProperty("java.version"), result.toString());
    }
    
    @Test
    void testResolveNonExistentProperty() {
        // 测试解析不存在的系统属性
        Object result = resolver.resolve("non.existent.property", null);
        assertNull(result);
    }
    
    @Test
    void testResolveWithDefaultValue() {
        // 设置一个测试属性
        System.setProperty("test.property", "test.value");
        
        // 测试解析存在的属性（应该返回实际值，不是默认值）
        Object result = resolver.resolve("test.property:default.value", null);
        assertEquals("test.value", result.toString());
        
        // 测试解析不存在的属性（应该返回默认值）
        result = resolver.resolve("non.existent.property:default.value", null);
        assertEquals("default.value", result.toString());
    }
    
    @Test
    void testResolveWithEmptyDefaultValue() {
        // 测试空默认值
        Object result = resolver.resolve("non.existent.property:", null);
        assertEquals("", result.toString());
    }
    
    @Test
    void testResolveWithColonInDefaultValue() {
        // 测试默认值中包含冒号的情况
        Object result = resolver.resolve("non.existent.property:http://localhost:8080", null);
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
    void testGetPriority() {
        // 测试优先级
        assertEquals(50, resolver.getPriority());
    }
    
    @Test
    void testGetName() {
        // 测试解析器名称
        assertEquals("SystemPropertyResolver", resolver.getName());
    }
    
    @Test
    void testResolveWithWhitespace() {
        // 设置测试属性
        System.setProperty("test.property", "test.value");
        
        // 测试变量名和默认值包含空格的情况
        Object result = resolver.resolve(" test.property : default.value ", null);
        assertEquals("test.value", result.toString());
        
        // 测试不存在的属性
        result = resolver.resolve(" non.existent.property : default.value ", null);
        assertEquals("default.value", result.toString());
    }
    
    @Test
    void testResolveSystemProperties() {
        // 测试一些常见的系统属性
        Object javaVersion = resolver.resolve("java.version", null);
        assertNotNull(javaVersion);
        
        Object osName = resolver.resolve("os.name", null);
        assertNotNull(osName);
        
        Object userHome = resolver.resolve("user.home", null);
        assertNotNull(userHome);
    }
}