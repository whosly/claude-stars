package com.yueny.stars.netty.monitor.agent.template.resolver;

import com.yueny.stars.netty.monitor.agent.template.VariableResolver;
import com.yueny.stars.netty.monitor.agent.util.Logger;

/**
 * 环境变量解析器
 * 解析环境变量，支持${env.VARIABLE_NAME}和${env.VARIABLE_NAME:default}语法
 * 
 * @author fengyang
 */
public class EnvironmentResolver implements VariableResolver {
    
    private static final Logger logger = Logger.getLogger(EnvironmentResolver.class);
    
    // 优先级：中等（40）
    private static final int PRIORITY = 40;
    
    // 环境变量前缀
    private static final String ENV_PREFIX = "env.";
    
    @Override
    public boolean canResolve(String variable) {
        if (variable == null || variable.trim().isEmpty()) {
            return false;
        }
        
        // 解析变量名（去除默认值部分）
        String variableName = variable.split(":", 2)[0].trim();
        
        // 检查是否以env.开头且后面有环境变量名
        if (!variableName.startsWith(ENV_PREFIX)) {
            return false;
        }
        
        // 提取环境变量名
        String envVarName = variableName.substring(ENV_PREFIX.length());
        
        // 环境变量名不能为空
        return !envVarName.isEmpty();
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
            
            // 检查是否以env.开头
            if (!variableName.startsWith(ENV_PREFIX)) {
                logger.debug("变量名不以env.开头: %s", variableName);
                return defaultValue;
            }
            
            // 提取环境变量名
            String envVarName = variableName.substring(ENV_PREFIX.length());
            
            if (envVarName.isEmpty()) {
                logger.debug("环境变量名为空: %s", variableName);
                return defaultValue;
            }
            
            // 获取环境变量
            String value = System.getenv(envVarName);
            
            if (value != null) {
                logger.debug("从环境变量获取: %s = %s", envVarName, value);
                return value;
            } else if (defaultValue != null) {
                logger.debug("环境变量 %s 不存在，使用默认值: %s", envVarName, defaultValue);
                return defaultValue;
            } else {
                logger.debug("环境变量 %s 不存在且无默认值", envVarName);
                return null;
            }
            
        } catch (Exception e) {
            logger.warn("解析环境变量 %s 时出错: %s", variable, e.getMessage());
            return null;
        }
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public String getName() {
        return "EnvironmentResolver";
    }
}