package com.whosly.stars.netty.chats;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author fengyang
 * @date 2025-08-22 10:52:28
 * @description
 */
@ToString
@Getter
@Setter
public class Message {
    private String message;

    private long timestamp;

    public Message(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 检查消息是否有效（非空且非空白）
     */
    public boolean isValid() {
        return message != null && !message.trim().isEmpty();
    }
    
    /**
     * 获取清理后的消息内容（去除前后空白）
     */
    public String getTrimmedMessage() {
        return message != null ? message.trim() : "";
    }
}
