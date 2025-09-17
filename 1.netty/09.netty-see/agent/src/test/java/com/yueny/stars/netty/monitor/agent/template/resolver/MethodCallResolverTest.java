package com.yueny.stars.netty.monitor.agent.template.resolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MethodCallResolver单元测试
 * 
 * @author fengyang
 */
class MethodCallResolverTest {
    
    private MethodCallResolver resolver;
    private TestObject testObject;
    
    @BeforeEach
    void setUp() {
        resolver = new MethodCallResolver();
        testObject = new TestObject();
    }
    
    @Test
    void testCanResolve() {
        // 测试能够识别方法调用语法
        assertTrue(resolver.canResolve("getName()"));
        assertTrue(resolver.canResolve("getAge():18"));
        assertTrue(resolver.canResolve("field.getValue()"));
        assertTrue(resolver.canResolve("nested.field.getInfo()"));
        
        // 测试不能识别非方法调用语法
        assertFalse(resolver.canResolve("getName"));
        assertFalse(resolver.canResolve("field.value"));
        assertFalse(resolver.canResolve("property"));
        
        // 测试边界情况
        assertFalse(resolver.canResolve(null));
        assertFalse(resolver.canResolve(""));
        assertFalse(resolver.canResolve("   "));
    }
    
    @Test
    void testResolveSimpleMethod() {
        // 测试简单方法调用
        Object result = resolver.resolve("getName()", testObject);
        assertEquals("TestName", result);
        
        result = resolver.resolve("getAge()", testObject);
        assertEquals(25, result);
        
        result = resolver.resolve("isActive()", testObject);
        assertEquals(true, result);
    }
    
    @Test
    void testResolveWithDefaultValue() {
        // 测试不存在的方法，使用默认值
        Object result = resolver.resolve("getNonExistent():defaultValue", testObject);
        assertEquals("defaultValue", result);
        
        // 测试存在的方法，不使用默认值
        result = resolver.resolve("getName():defaultValue", testObject);
        assertEquals("TestName", result);
    }
    
    @Test
    void testResolveFieldMethod() {
        // 测试字段方法调用
        Object result = resolver.resolve("nestedObject.getValue()", testObject);
        assertEquals("NestedValue", result);
        
        result = resolver.resolve("nestedObject.getCount()", testObject);
        assertEquals(42, result);
    }
    
    @Test
    void testResolveDeepFieldMethod() {
        // 测试深层字段方法调用
        Object result = resolver.resolve("nestedObject.deepObject.getDeepValue()", testObject);
        assertEquals("DeepValue", result);
    }
    
    @Test
    void testResolveNullInstance() {
        // 测试null实例
        Object result = resolver.resolve("getName()", null);
        assertNull(result);
    }
    
    @Test
    void testResolveNullVariable() {
        // 测试null变量
        Object result = resolver.resolve(null, testObject);
        assertNull(result);
    }
    
    @Test
    void testResolveEmptyVariable() {
        // 测试空变量
        Object result = resolver.resolve("", testObject);
        assertNull(result);
        
        result = resolver.resolve("   ", testObject);
        assertNull(result);
    }
    
    @Test
    void testResolveInvalidMethodFormat() {
        // 测试无效的方法格式
        Object result = resolver.resolve("getName", testObject);
        assertNull(result);
        
        result = resolver.resolve("getName(", testObject);
        assertNull(result);
        
        result = resolver.resolve("getName)", testObject);
        assertNull(result);
    }
    
    @Test
    void testResolveNonExistentMethod() {
        // 测试不存在的方法
        Object result = resolver.resolve("getNonExistent()", testObject);
        assertNull(result);
    }
    
    @Test
    void testResolveNonExistentField() {
        // 测试不存在的字段
        Object result = resolver.resolve("nonExistentField.getValue()", testObject);
        assertNull(result);
    }
    
    @Test
    void testResolveNullField() {
        // 测试null字段
        Object result = resolver.resolve("nullField.getValue()", testObject);
        assertNull(result);
    }
    
    @Test
    void testResolveMethodReturningNull() {
        // 测试返回null的方法
        Object result = resolver.resolve("getNullValue()", testObject);
        assertNull(result);
        
        // 测试返回null的方法，使用默认值
        result = resolver.resolve("getNullValue():defaultValue", testObject);
        assertEquals("defaultValue", result);
    }
    
    @Test
    void testGetPriority() {
        // 测试优先级
        assertEquals(20, resolver.getPriority());
    }
    
    @Test
    void testGetName() {
        // 测试解析器名称
        assertEquals("MethodCallResolver", resolver.getName());
    }
    
    @Test
    void testResolveWithWhitespace() {
        // 测试包含空格的变量
        Object result = resolver.resolve(" getName() : defaultValue ", testObject);
        assertEquals("TestName", result);
        
        result = resolver.resolve(" getNonExistent() : defaultValue ", testObject);
        assertEquals("defaultValue", result);
    }
    
    @Test
    void testResolveWithColonInDefaultValue() {
        // 测试默认值中包含冒号的情况
        Object result = resolver.resolve("getNonExistent():http://localhost:8080", testObject);
        assertEquals("http://localhost:8080", result);
    }
    
    // 测试用的内部类
    public static class TestObject {
        private String name = "TestName";
        private int age = 25;
        private boolean active = true;
        private NestedObject nestedObject = new NestedObject();
        private Object nullField = null;
        
        public String getName() {
            return name;
        }
        
        public int getAge() {
            return age;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public NestedObject getNestedObject() {
            return nestedObject;
        }
        
        public Object getNullValue() {
            return null;
        }
    }
    
    public static class NestedObject {
        private String value = "NestedValue";
        private int count = 42;
        private DeepObject deepObject = new DeepObject();
        
        public String getValue() {
            return value;
        }
        
        public int getCount() {
            return count;
        }
        
        public DeepObject getDeepObject() {
            return deepObject;
        }
    }
    
    public static class DeepObject {
        private String deepValue = "DeepValue";
        
        public String getDeepValue() {
            return deepValue;
        }
    }
}