package com.yueny.stars.netty.monitor.agent.template;

import com.yueny.stars.netty.monitor.agent.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板解析器
 * 负责解析包含变量的模板字符串，支持${variable}和${variable:default}语法
 * 
 * @author fengyang
 */
public class TemplateResolver {
    
    private static final Logger logger = Logger.getLogger(TemplateResolver.class);
    
    // 模板变量正则表达式：${variable} 或 ${variable:defaultValue}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    // 变量解析器列表（按优先级排序）
    private final List<VariableResolver> resolvers;
    
    // 解析结果缓存
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    // 是否启用缓存
    private volatile boolean cacheEnabled = true;
    
    /**
     * 构造函数
     * 
     * @param resolvers 变量解析器列表
     */
    public TemplateResolver(List<VariableResolver> resolvers) {
        if (resolvers == null || resolvers.isEmpty()) {
            this.resolvers = new ArrayList<>();
        } else {
            // 按优先级排序
            this.resolvers = new ArrayList<>(resolvers);
            this.resolvers.sort((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()));
        }
        
        logger.debug("模板解析器初始化，包含 %d 个变量解析器", this.resolvers.size());
    }
    
    /**
     * 解析模板字符串
     * 
     * @param template 模板字符串
     * @param instance 当前对象实例
     * @return 解析后的字符串
     */
    public String resolve(String template, Object instance) {
        if (template == null || template.trim().isEmpty()) {
            return template;
        }
        
        // 检查缓存
        String cacheKey = template + "@" + (instance != null ? instance.getClass().getName() : "null");
        if (cacheEnabled) {
            String cached = cache.get(cacheKey);
            if (cached != null) {
                logger.debug("从缓存获取模板解析结果: %s -> %s", template, cached);
                return cached;
            }
        }
        
        String result = resolveInternal(template, instance);
        
        // 缓存结果
        if (cacheEnabled && result != null) {
            cache.put(cacheKey, result);
        }
        
        logger.debug("模板解析完成: %s -> %s", template, result);
        return result;
    }
    
    /**
     * 内部解析逻辑
     */
    private String resolveInternal(String template, Object instance) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableExpression = matcher.group(1); // 获取${...}中的内容
            String resolvedValue = resolveVariable(variableExpression, instance);
            
            // 替换变量
            if (resolvedValue != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(resolvedValue));
            } else {
                // 如果无法解析，保留原始变量表达式
                logger.warn("无法解析变量: ${%s}，保留原始表达式", variableExpression);
                matcher.appendReplacement(result, Matcher.quoteReplacement("${" + variableExpression + "}"));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 解析单个变量
     */
    private String resolveVariable(String variableExpression, Object instance) {
        // 解析变量名和默认值
        String[] parts = variableExpression.split(":", 2);
        String variableName = parts[0].trim();
        String defaultValue = parts.length > 1 ? parts[1].trim() : null;
        
        logger.debug("解析变量: %s (默认值: %s)", variableName, defaultValue);
        
        // 尝试使用各个解析器解析
        for (VariableResolver resolver : resolvers) {
            if (resolver.canResolve(variableName)) {
                try {
                    Object value = resolver.resolve(variableExpression, instance);
                    if (value != null) {
                        String stringValue = value.toString();
                        logger.debug("变量 %s 由 %s 解析为: %s", variableName, resolver.getName(), stringValue);
                        return stringValue;
                    }
                } catch (Exception e) {
                    logger.warn("解析器 %s 解析变量 %s 时出错: %s", resolver.getName(), variableName, e.getMessage());
                }
            }
        }
        
        // 如果所有解析器都无法解析，返回默认值
        if (defaultValue != null) {
            logger.debug("使用默认值解析变量 %s: %s", variableName, defaultValue);
            return defaultValue;
        }
        
        logger.debug("变量 %s 无法解析且无默认值", variableName);
        return null;
    }
    
    /**
     * 验证模板语法
     * 
     * @param template 模板字符串
     * @return 验证结果
     */
    public ValidationResult validate(String template) {
        if (template == null) {
            return new ValidationResult(false, "模板不能为null");
        }
        
        if (template.trim().isEmpty()) {
            return new ValidationResult(true, "空模板有效");
        }
        
        try {
            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            List<String> variables = new ArrayList<>();
            
            while (matcher.find()) {
                String variableExpression = matcher.group(1);
                variables.add(variableExpression);
                
                // 检查变量表达式格式
                if (variableExpression.trim().isEmpty()) {
                    return new ValidationResult(false, "发现空变量表达式: ${}");
                }
                
                // 检查是否有未闭合的大括号
                if (variableExpression.contains("{") || variableExpression.contains("}")) {
                    return new ValidationResult(false, "变量表达式包含非法字符: " + variableExpression);
                }
            }
            
            return new ValidationResult(true, "模板语法有效，包含 " + variables.size() + " 个变量: " + variables);
            
        } catch (Exception e) {
            return new ValidationResult(false, "模板语法错误: " + e.getMessage());
        }
    }
    
    /**
     * 添加变量解析器
     * 
     * @param resolver 变量解析器
     */
    public void addResolver(VariableResolver resolver) {
        if (resolver != null) {
            resolvers.add(resolver);
            // 重新排序
            resolvers.sort((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()));
            logger.debug("添加变量解析器: %s (优先级: %d)", resolver.getName(), resolver.getPriority());
        }
    }
    
    /**
     * 移除变量解析器
     * 
     * @param resolverClass 解析器类
     */
    public void removeResolver(Class<? extends VariableResolver> resolverClass) {
        resolvers.removeIf(resolver -> resolver.getClass().equals(resolverClass));
        logger.debug("移除变量解析器: %s", resolverClass.getSimpleName());
    }
    
    /**
     * 获取所有解析器的只读列表
     * 
     * @return 解析器列表
     */
    public List<VariableResolver> getResolvers() {
        return Collections.unmodifiableList(resolvers);
    }
    
    /**
     * 启用或禁用缓存
     * 
     * @param enabled 是否启用缓存
     */
    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        if (!enabled) {
            cache.clear();
        }
        logger.debug("模板解析缓存: %s", enabled ? "启用" : "禁用");
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        cache.clear();
        logger.debug("清空模板解析缓存");
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存条目数量
     */
    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", message='" + message + "'}";
        }
    }
}