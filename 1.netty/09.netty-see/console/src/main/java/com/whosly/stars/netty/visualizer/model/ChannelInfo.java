package com.whosly.stars.netty.visualizer.model;

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
    private String errorMessage;     // 错误消息
    private String errorType;        // 错误类型
    private String stackTrace;       // 堆栈跟踪
    private String username;         // 用户名信息
    private String channelRole;      // channel角色：CLIENT 或 SERVER
    private java.util.Map<String, Object> bufferInfo;  // 缓冲区信息
}