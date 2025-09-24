package com.whosly.stars.netty.monitor.agent.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NettyMonitor注解测试
 * 
 * @author fengyang
 */
class NettyMonitorTest {
    
    @Test
    void testDefaultValues() throws Exception {
        // 获取注解的默认值
        Method[] methods = NettyMonitor.class.getDeclaredMethods();
        
        // 验证各个属性的默认值
        for (Method method : methods) {
            Object defaultValue = method.getDefaultValue();
            String methodName = method.getName();
            
            switch (methodName) {
                case "value":
                    assertEquals("", defaultValue);
                    break;
                case "applicationName":
                    assertEquals("${class.simpleName}", defaultValue);
                    break;
                case "host":
                    assertEquals("${monitor.host:localhost}", defaultValue);
                    break;
                case "port":
                    assertEquals(19999, defaultValue);
                    break;
                case "enabled":
                    assertEquals(true, defaultValue);
                    break;
                case "lazyInit":
                    assertEquals(true, defaultValue);
                    break;
                case "initTimeout":
                    assertEquals(5000, defaultValue);
                    break;
                case "connectTimeout":
                    assertEquals(5000, defaultValue);
                    break;
                case "reconnectInterval":
                    assertEquals(5, defaultValue);
                    break;
                case "retryCount":
                    assertEquals(3, defaultValue);
                    break;
                case "retryInterval":
                    assertEquals(1000, defaultValue);
                    break;
            }
        }
    }
    
    @Test
    void testAnnotationOnClass() {
        // 测试类级别注解
        TestClassWithDefaults testClass = new TestClassWithDefaults();
        NettyMonitor annotation = testClass.getClass().getAnnotation(NettyMonitor.class);
        
        assertNotNull(annotation);
        assertEquals("", annotation.value());
        assertEquals("${class.simpleName}", annotation.applicationName());
        assertEquals("${monitor.host:localhost}", annotation.host());
        assertEquals(19999, annotation.port());
        assertTrue(annotation.enabled());
        assertTrue(annotation.lazyInit());
        assertEquals(5000, annotation.initTimeout());
        assertEquals(5000, annotation.connectTimeout());
        assertEquals(5, annotation.reconnectInterval());
        assertEquals(3, annotation.retryCount());
        assertEquals(1000, annotation.retryInterval());
    }
    
    @Test
    void testAnnotationWithCustomValues() {
        // 测试自定义值注解
        TestClassWithCustomValues testClass = new TestClassWithCustomValues();
        NettyMonitor annotation = testClass.getClass().getAnnotation(NettyMonitor.class);
        
        assertNotNull(annotation);
        assertEquals("CustomApp", annotation.value());
        assertEquals("CustomApp-${username}", annotation.applicationName());
        assertEquals("${custom.host:192.168.1.100}", annotation.host());
        assertEquals(8080, annotation.port());
        assertFalse(annotation.enabled());
        assertFalse(annotation.lazyInit());
        assertEquals(10000, annotation.initTimeout());
        assertEquals(3000, annotation.connectTimeout());
        assertEquals(10, annotation.reconnectInterval());
        assertEquals(5, annotation.retryCount());
        assertEquals(2000, annotation.retryInterval());
    }
    
    @Test
    void testAnnotationOnMethod() throws Exception {
        // 测试方法级别注解
        Method method = TestClassWithMethodAnnotation.class.getMethod("monitoredMethod");
        NettyMonitor annotation = method.getAnnotation(NettyMonitor.class);
        
        assertNotNull(annotation);
        assertEquals("MethodApp", annotation.applicationName());
        assertEquals("${method.host:localhost}", annotation.host());
        assertEquals(9999, annotation.port());
    }
    
    @Test
    void testTemplateExamples() {
        // 测试各种模板语法示例
        TestClassWithTemplates testClass = new TestClassWithTemplates();
        NettyMonitor annotation = testClass.getClass().getAnnotation(NettyMonitor.class);
        
        assertNotNull(annotation);
        // 验证模板语法格式正确
        assertTrue(annotation.applicationName().contains("${"));
        assertTrue(annotation.applicationName().contains("}"));
        assertTrue(annotation.host().contains("${"));
        assertTrue(annotation.host().contains("}"));
    }
    
    // 测试用的类
    @NettyMonitor
    static class TestClassWithDefaults {
    }
    
    @NettyMonitor(
        value = "CustomApp",
        applicationName = "CustomApp-${username}",
        host = "${custom.host:192.168.1.100}",
        port = 8080,
        enabled = false,
        lazyInit = false,
        initTimeout = 10000,
        connectTimeout = 3000,
        reconnectInterval = 10,
        retryCount = 5,
        retryInterval = 2000
    )
    static class TestClassWithCustomValues {
    }
    
    static class TestClassWithMethodAnnotation {
        @NettyMonitor(
            applicationName = "MethodApp",
            host = "${method.host:localhost}",
            port = 9999
        )
        public void monitoredMethod() {
        }
    }
    
    @NettyMonitor(
        applicationName = "App-${username}-${server.port}",
        host = "${env.MONITOR_HOST:${system.monitor.host:localhost}}"
    )
    static class TestClassWithTemplates {
    }
}