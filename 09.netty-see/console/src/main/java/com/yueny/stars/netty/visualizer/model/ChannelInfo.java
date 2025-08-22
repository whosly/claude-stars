package com.yueny.stars.netty.visualizer.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Channel信息模型
 * 
 * @author fengyang
 */
@Data
public class ChannelInfo {
    private String channelId;
    private String remoteAddress;
    private String localAddress;
    private boolean active;
    private boolean open;
    private boolean writable;
    private String state;
    private LocalDateTime createTime;
    private LocalDateTime lastActiveTime;
    private long bytesRead;
    private long bytesWritten;
    private int messagesRead;
    private int messagesWritten;
    private String eventLoopGroup;
    private String pipeline;
    private String applicationName;  // 添加应用名称字段
}