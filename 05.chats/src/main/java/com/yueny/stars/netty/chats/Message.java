package com.yueny.stars.netty.chats;

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
}
