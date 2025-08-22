package com.yueny.stars.netty.visualizer.model;

import lombok.Data;

/**
 * 缓冲区信息模型
 * 
 * @author fengyang
 */
@Data
public class BufferInfo {
    private String channelId;
    private int capacity;
    private int readableBytes;
    private int writableBytes;
    private int readerIndex;
    private int writerIndex;
    private boolean isDirect;
    private int refCount;
    private String content;
}