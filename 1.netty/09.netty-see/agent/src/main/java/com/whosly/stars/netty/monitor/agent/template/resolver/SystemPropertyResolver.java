package com.whosly.stars.netty.monitor.agent.template.resolver;

import com.whosly.stars.netty.monitor.agent.template.VariableResolver;
import com.whosly.stars.netty.monitor.agent.util.Logger;

/**
 * 系统属性变量解析器
 * 解析系统属性变量，支持默认值
 * 
 * @author fengyang
 */
public class SystemPropertyResolver implements VariableResolver {
    
    private static final Logger logger = Logger.getLogger(SystemPropertyResolver.class);
    
    // 优先级：中等（50）
    private static final int PRIORITY = 50;
    
    @Override
    public boolean canResolve(String variable) {
        if (variable == null || variable.trim().isEmpty()) {
            return false;
        }
        
        // 支持所有变量，因为系统属性可以是任意名称
        // 但优先级较低，让更具体的解析器先处理
        return true;
    }
    
    @Override
    public Object resolve(String variable, Object instance) {
        if (variable == null || variable.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 解析变量名和默认值
            String[] parts = variable.split(":", 2);
            String propertyName = parts[0].trim();
            String defaultValue = parts.length > 1 ? parts[1].trim() : null;
            
            // 获取系统属性
            String value = System.getProperty(propertyName);
            
            if (value != null) {
                logger.debug("从系统属性获取: %s = %s", propertyName, value);
                return value;
            } else if (defaultValue != null) {
                logger.debug("系统属性 %s 不存在，使用默认值: %s", propertyName, defaultValue);
                return defaultValue;
            } else {
                logger.debug("系统属性 %s 不存在且无默认值", propertyName);
                return null;
            }
            
        } catch (Exception e) {
            logger.warn("解析系统属性变量 %s 时出错: %s", variable, e.getMessage());
            return null;
        }
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public String getName() {
        return "SystemPropertyResolver";
    }
}