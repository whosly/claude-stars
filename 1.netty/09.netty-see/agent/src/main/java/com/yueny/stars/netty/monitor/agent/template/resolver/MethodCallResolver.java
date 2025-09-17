package com.yueny.stars.netty.monitor.agent.template.resolver;

import com.yueny.stars.netty.monitor.agent.template.VariableResolver;
import com.yueny.stars.netty.monitor.agent.util.Logger;

import java.lang.reflect.Method;

/**
 * 方法调用变量解析器
 * 支持调用对象的方法获取变量值，如${methodName()}或${field.methodName()}
 * 
 * @author fengyang
 */
public class MethodCallResolver implements VariableResolver {
    
    private static final Logger logger = Logger.getLogger(MethodCallResolver.class);
    
    // 优先级：中高（20）
    private static final int PRIORITY = 20;
    
    @Override
    public boolean canResolve(String variable) {
        if (variable == null || variable.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含方法调用语法 ()
        String variableName = variable.split(":", 2)[0].trim();
        return variableName.contains("()");
    }
    
    @Override
    public Object resolve(String variable, Object instance) {
        if (variable == null || variable.trim().isEmpty() || instance == null) {
            return null;
        }
        
        try {
            // 解析变量名和默认值
            String[] parts = variable.split(":", 2);
            String methodExpression = parts[0].trim();
            String defaultValue = parts.length > 1 ? parts[1].trim() : null;
            
            // 移除括号
            if (!methodExpression.endsWith("()")) {
                logger.debug("方法表达式格式错误: %s", methodExpression);
                return defaultValue;
            }
            
            String methodName = methodExpression.substring(0, methodExpression.length() - 2);
            
            // 处理字段访问，如 field.methodName
            Object targetObject = instance;
            String[] fieldPath = methodName.split("\\.");
            
            // 如果有字段路径，先获取字段对象
            for (int i = 0; i < fieldPath.length - 1; i++) {
                targetObject = getFieldValue(targetObject, fieldPath[i]);
                if (targetObject == null) {
                    logger.debug("字段路径中断: %s", fieldPath[i]);
                    return defaultValue;
                }
            }
            
            // 获取最终的方法名
            String finalMethodName = fieldPath[fieldPath.length - 1];
            
            // 调用方法
            Object result = invokeMethod(targetObject, finalMethodName);
            
            if (result != null) {
                logger.debug("方法调用成功: %s = %s", methodExpression, result);
                return result;
            } else if (defaultValue != null) {
                logger.debug("方法调用返回null，使用默认值: %s", defaultValue);
                return defaultValue;
            } else {
                logger.debug("方法调用返回null且无默认值: %s", methodExpression);
                return null;
            }
            
        } catch (Exception e) {
            logger.warn("解析方法调用变量 %s 时出错: %s", variable, e.getMessage());
            
            // 解析默认值
            String[] parts = variable.split(":", 2);
            if (parts.length > 1) {
                return parts[1].trim();
            }
            return null;
        }
    }
    
    /**
     * 获取字段值
     */
    private Object getFieldValue(Object object, String fieldName) {
        if (object == null || fieldName == null) {
            return null;
        }
        
        try {
            Class<?> clazz = object.getClass();
            
            // 尝试通过getter方法获取
            String getterName = "get" + capitalize(fieldName);
            try {
                Method getter = clazz.getMethod(getterName);
                return getter.invoke(object);
            } catch (NoSuchMethodException e) {
                // 尝试boolean类型的getter
                String booleanGetterName = "is" + capitalize(fieldName);
                try {
                    Method booleanGetter = clazz.getMethod(booleanGetterName);
                    return booleanGetter.invoke(object);
                } catch (NoSuchMethodException e2) {
                    // 尝试直接访问字段
                    try {
                        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        return field.get(object);
                    } catch (Exception e3) {
                        logger.debug("无法获取字段值: %s.%s", clazz.getSimpleName(), fieldName);
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("获取字段值时出错: %s", e.getMessage());
            return null;
        }
    }
    
    /**
     * 调用方法
     */
    private Object invokeMethod(Object object, String methodName) {
        if (object == null || methodName == null) {
            return null;
        }
        
        try {
            Class<?> clazz = object.getClass();
            Method method = clazz.getMethod(methodName);
            return method.invoke(object);
        } catch (Exception e) {
            logger.debug("调用方法失败: %s.%s() - %s", 
                    object.getClass().getSimpleName(), methodName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public String getName() {
        return "MethodCallResolver";
    }
}