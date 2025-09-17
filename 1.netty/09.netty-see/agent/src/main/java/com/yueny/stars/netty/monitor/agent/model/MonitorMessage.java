package com.yueny.stars.netty.monitor.agent.model;

/**
 * 监控消息模型
 * 
 * @author fengyang
 */
public class MonitorMessage {
    
    /**
     * 消息类型
     * APP_REGISTER - 应用注册
     * CHANNEL_ACTIVE - Channel激活
     * CHANNEL_INACTIVE - Channel非激活
     * CHANNEL_READ - Channel读取数据
     * CHANNEL_WRITE - Channel写入数据
     */
    private String type;
    
    /**
     * 应用名称
     */
    private String applicationName;
    
    /**
     * Channel信息
     */
    private ChannelInfo channelInfo;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 额外数据
     */
    private Object data;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }

    public ChannelInfo getChannelInfo() { return channelInfo; }
    public void setChannelInfo(ChannelInfo channelInfo) { this.channelInfo = channelInfo; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}