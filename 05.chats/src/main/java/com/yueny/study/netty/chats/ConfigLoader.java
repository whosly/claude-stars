package com.yueny.study.netty.chats;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 聊天应用配置读取工具类
 * @author fengyang
 * @date 2025-08-18
 */
public class ConfigLoader {
    
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";
    
    static {
        loadProperties();
    }
    
    /**
     * 加载配置文件
     */
    private static void loadProperties() {
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("配置文件 " + CONFIG_FILE + " 未找到，使用默认配置");
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage() + "，使用默认配置");
            setDefaultProperties();
        }
    }
    
    /**
     * 设置默认配置
     */
    private static void setDefaultProperties() {
        properties.setProperty("server.port", "8080");
        properties.setProperty("server.host", "localhost");
    }
    
    /**
     * 获取服务器端口
     */
    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8080"));
    }
    
    /**
     * 获取服务器主机
     */
    public static String getServerHost() {
        return properties.getProperty("server.host", "localhost");
    }

}
