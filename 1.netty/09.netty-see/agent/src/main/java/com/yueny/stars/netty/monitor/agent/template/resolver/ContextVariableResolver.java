package com.yueny.stars.netty.monitor.agent.template.resolver;

import com.yueny.stars.netty.monitor.agent.context.MonitorContextManager;
import com.yueny.stars.netty.monitor.agent.template.VariableResolver;
import com.yueny.stars.netty.monitor.agent.util.Logger;

/**
 * 上下文变量解析器
 * 从MonitorContextManager获取变量值，支持线程本地和全局上下文
 * 
 * @author fengyang
 */
public class ContextVariableResolver implements VariableResolver {
    
    private static final Logger logger = Logger.getLogger(ContextVariableResolver.class);
    
    // 优先级：高（10）
    private static final int PRIORITY = 10;
    
    @Override
    public boolean canResolve(String variable) {
        if (variable == null || variable.trim().isEmpty()) {
            return false;
        }
        
        // 解析变量名（去除默认值部分）
        String variableName = variable.split(":", 2)[0].trim();
        
        // 检查上下文中是否存在该变量
        Object value = MonitorContextManager.getContext(variableName);
        return value != null;
    }
    
    @Override
    public Object resolve(String variable, Object instance) {
        if (variable == null || variable.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 解析变量名和默认值
            String[] parts = variable.split(":", 2);
            String variableName = parts[0].trim();
            String defaultValue = parts.length > 1 ? parts[1].trim() : null;
            
            // 从上下文获取变量值
            Object value = MonitorContextManager.getContext(variableName);
            
            if (value != null) {
                logger.debug("从上下文获取变量: %s = %s", variableName, value);
                return value;
            } else if (defaultValue != null) {
                logger.debug("上下文变量 %s 不存在，使用默认值: %s", variableName, defaultValue);
                return defaultValue;
            } else {
                logger.debug("上下文变量 %s 不存在且无默认值", variableName);
                return null;
            }
            
        } catch (Exception e) {
            logger.warn("解析上下文变量 %s 时出错: %s", variable, e.getMessage());
            return null;
        }
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public String getName() {
        return "ContextVariableResolver";
    }
}