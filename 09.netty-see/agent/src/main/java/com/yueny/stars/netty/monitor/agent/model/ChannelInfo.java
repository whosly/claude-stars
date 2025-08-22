package com.yueny.stars.netty.monitor.agent.model;

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
    private long timestamp;

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

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}