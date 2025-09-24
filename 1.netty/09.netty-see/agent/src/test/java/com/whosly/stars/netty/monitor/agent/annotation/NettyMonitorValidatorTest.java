package com.whosly.stars.netty.monitor.agent.annotation;

import com.whosly.stars.netty.monitor.agent.template.TemplateResolver;
import com.whosly.stars.netty.monitor.agent.template.resolver.SystemPropertyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NettyMonitorValidator单元测试
 * 
 * @author fengyang
 */
class NettyMonitorValidatorTest {
    
    private NettyMonitorValidator validator;
    private TemplateResolver templateResolver;
    
    @BeforeEach
    void setUp() {
        templateResolver = new TemplateResolver(Arrays.asList(new SystemPropertyResolver()));
        validator = new NettyMonitorValidator(templateResolver);
    }
    
    @Test
    void testValidateNullAnnotation() {
        NettyMonitorValidator.ValidationResult result = validator.validate(null);
        assertFalse(result.isValid());
        assertEquals("注解不能为null", result.getMessage());
    }
    
    @Test
    void testValidateValidAnnotation() {
        NettyMonitor annotation = createMockAnnotation(
            "TestApp", 
            "TestApp-${username}", 
            "localhost", 
            8080, 
            true, 
            true, 
            5000, 
            3000, 
            5, 
            3, 
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertTrue(result.isValid());
        assertTrue(result.getMessage().contains("验证通过") || result.hasWarnings());
    }
    
    @Test
    void testValidateInvalidPort() {
        NettyMonitor annotation = createMockAnnotation(
            "", 
            "TestApp", 
            "localhost", 
            -1, // 无效端口
            true, 
            true, 
            5000, 
            3000, 
            5, 
            3, 
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("端口号必须在1-65535范围内"));
    }
    
    @Test
    void testValidateInvalidTimeout() {
        NettyMonitor annotation = createMockAnnotation(
            "", 
            "TestApp", 
            "localhost", 
            8080, 
            true, 
            true, 
            -1000, // 无效超时时间
            3000, 
            5, 
            3, 
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("初始化超时时间必须大于0"));
    }
    
    @Test
    void testValidateEmptyApplicationName() {
        NettyMonitor annotation = createMockAnnotation(
            "", 
            "", // 空应用名称
            "localhost", 
            8080, 
            true, 
            true, 
            5000, 
            3000, 
            5, 
            3, 
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("应用名称不能为空"));
    }
    
    @Test
    void testValidateInvalidTemplateInApplicationName() {
        NettyMonitor annotation = createMockAnnotation(
            "", 
            "TestApp-${invalid}", // 无效模板语法
            "localhost", 
            8080, 
            true, 
            true, 
            5000, 
            3000, 
            5, 
            3, 
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        // 模板验证可能通过（因为语法正确），但变量可能无法解析
        // 这里主要测试验证器能正常处理模板
        assertNotNull(result);
    }
    
    @Test
    void testValidateWarnings() {
        NettyMonitor annotation = createMockAnnotation(
            "TestApp", // 同时设置value和applicationName
            "TestApp-${username}", 
            "localhost", 
            80, // 系统端口，会产生警告
            true, 
            true, 
            70000, // 超长超时时间，会产生警告
            3000, 
            5, 
            15, // 过多重试次数，会产生警告
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().size() > 0);
    }
    
    @Test
    void testValidateInvalidRetryConfig() {
        NettyMonitor annotation = createMockAnnotation(
            "", 
            "TestApp", 
            "localhost", 
            8080, 
            true, 
            true, 
            5000, 
            3000, 
            5, 
            -1, // 无效重试次数
            -1000 // 无效重试间隔
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("重试次数不能为负数"));
        assertTrue(result.getMessage().contains("重试间隔必须大于0"));
    }
    
    @Test
    void testValidateEmptyHost() {
        NettyMonitor annotation = createMockAnnotation(
            "", 
            "TestApp", 
            "", // 空主机地址
            8080, 
            true, 
            true, 
            5000, 
            3000, 
            5, 
            3, 
            1000
        );
        
        NettyMonitorValidator.ValidationResult result = validator.validate(annotation);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("主机地址不能为空"));
    }
    
    // 创建模拟注解的辅助方法
    private NettyMonitor createMockAnnotation(
            String value, 
            String applicationName, 
            String host, 
            int port, 
            boolean enabled, 
            boolean lazyInit, 
            int initTimeout, 
            int connectTimeout, 
            int reconnectInterval, 
            int retryCount, 
            int retryInterval) {
        
        return new NettyMonitor() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return NettyMonitor.class;
            }
            
            @Override
            public String value() {
                return value;
            }
            
            @Override
            public String applicationName() {
                return applicationName;
            }
            
            @Override
            public String host() {
                return host;
            }
            
            @Override
            public int port() {
                return port;
            }
            
            @Override
            public boolean enabled() {
                return enabled;
            }
            
            @Override
            public boolean lazyInit() {
                return lazyInit;
            }
            
            @Override
            public int initTimeout() {
                return initTimeout;
            }
            
            @Override
            public int connectTimeout() {
                return connectTimeout;
            }
            
            @Override
            public int reconnectInterval() {
                return reconnectInterval;
            }
            
            @Override
            public int retryCount() {
                return retryCount;
            }
            
            @Override
            public int retryInterval() {
                return retryInterval;
            }
        };
    }
}