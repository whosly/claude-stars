package com.yueny.stars.netty.monitor.agent.model;

import java.util.Map;

/**
 * Channel信息模型
 * 
 * @author fengyang
 */
public class ChannelInfo {
    private String channelId;
    private String remoteAddress;
    private String localAddress;
    private boolean active;
    private boolean open;
    private boolean writable;
    private String state;
    private long createTime;
    private long lastActiveTime;
    private long bytesRead;
    private long bytesWritten;
    private int messagesRead;
    private int messagesWritten;
    private String eventLoopGroup;
    private String pipeline;
    private String errorMessage;
    private String errorType;
    private String stackTrace;
    private long timestamp;
    private String username;  // 用户名信息
    private String channelRole;  // channel角色：CLIENT 或 SERVER

    /**
     * 缓冲区信息
     */
    private Map<String, Object> bufferInfo;

    // Getters and Setters
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getRemoteAddress() { return remoteAddress; }
    public void setRemoteAddress(String remoteAddress) { this.remoteAddress = remoteAddress; }

    public String getLocalAddress() { return localAddress; }
    public void setLocalAddress(String localAddress) { this.localAddress = localAddress; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public boolean isWritable() { return writable; }
    public void setWritable(boolean writable) { this.writable = writable; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }

    public long getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(long lastActiveTime) { this.lastActiveTime = lastActiveTime; }

    public long getBytesRead() { return bytesRead; }
    public void setBytesRead(long bytesRead) { this.bytesRead = bytesRead; }

    public long getBytesWritten() { return bytesWritten; }
    public void setBytesWritten(long bytesWritten) { this.bytesWritten = bytesWritten; }

    public int getMessagesRead() { return messagesRead; }
    public void setMessagesRead(int messagesRead) { this.messagesRead = messagesRead; }

    public int getMessagesWritten() { return messagesWritten; }
    public void setMessagesWritten(int messagesWritten) { this.messagesWritten = messagesWritten; }

    public String getEventLoopGroup() { return eventLoopGroup; }
    public void setEventLoopGroup(String eventLoopGroup) { this.eventLoopGroup = eventLoopGroup; }

    public String getPipeline() { return pipeline; }
    public void setPipeline(String pipeline) { this.pipeline = pipeline; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getChannelRole() { return channelRole; }
    public void setChannelRole(String channelRole) { this.channelRole = channelRole; }

    public Map<String, Object> getBufferInfo() {
        return bufferInfo;
    }

    public void setBufferInfo(Map<String, Object> bufferInfo) {
        this.bufferInfo = bufferInfo;
    }
}