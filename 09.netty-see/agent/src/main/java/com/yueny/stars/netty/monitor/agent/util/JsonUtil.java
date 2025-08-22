package com.yueny.stars.netty.monitor.agent.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单的JSON工具类，不依赖外部库
 * 
 * @author fengyang
 */
public class JsonUtil {
    
    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }
        
        // 对于其他对象，使用反射获取字段
        return objectToJson(obj);
    }
    
    /**
     * Map转JSON
     */
    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(toJson(entry.getValue()));
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 对象转JSON（简单实现）
     */
    private static String objectToJson(Object obj) {
        Map<String, Object> map = new HashMap<>();
        
        try {
            // 使用反射获取字段
            java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                map.put(field.getName(), value);
            }
        } catch (Exception e) {
            // 如果反射失败，返回toString
            return "\"" + escapeString(obj.toString()) + "\"";
        }
        
        return mapToJson(map);
    }
    
    /**
     * 转义字符串
     */
    private static String escapeString(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 创建JSON对象构建器
     */
    public static JsonBuilder builder() {
        return new JsonBuilder();
    }
    
    /**
     * JSON构建器
     */
    public static class JsonBuilder {
        private final Map<String, Object> map = new HashMap<>();
        
        public JsonBuilder put(String key, Object value) {
            map.put(key, value);
            return this;
        }
        
        public String build() {
            return mapToJson(map);
        }
    }
}