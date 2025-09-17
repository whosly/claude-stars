package com.yueny.stars.netty.monitor.agent.template;

/**
 * 变量解析器接口
 * 用于解析模板中的变量，支持不同类型的变量源
 * 
 * @author fengyang
 */
public interface VariableResolver {
    
    /**
     * 检查是否能解析指定的变量
     * 
     * @param variable 变量名
     * @return 如果能解析返回true，否则返回false
     */
    boolean canResolve(String variable);
    
    /**
     * 解析变量值
     * 
     * @param variable 变量名（可能包含默认值，如 "key:defaultValue"）
     * @param instance 当前对象实例，用于方法调用等场景
     * @return 解析后的值，如果无法解析返回null
     */
    Object resolve(String variable, Object instance);
    
    /**
     * 获取解析器优先级
     * 数值越小优先级越高
     * 
     * @return 优先级数值
     */
    int getPriority();
    
    /**
     * 获取解析器名称，用于调试和日志
     * 
     * @return 解析器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}