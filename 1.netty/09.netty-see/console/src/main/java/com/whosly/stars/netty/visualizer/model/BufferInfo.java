package com.whosly.stars.netty.visualizer.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 缓冲区信息模型
 * 
 * @author fengyang
 */
@Data
public class BufferInfo {
    private String channelId;
    private String applicationName;
    private int capacity;
    private int maxCapacity;
    private int readableBytes;
    private int writableBytes;
    private int readerIndex;
    private int writerIndex;
    private boolean isDirect;
    private boolean hasArray;
    private int refCount;
    private String content;
    private String bufferType;
    private LocalDateTime lastUpdateTime;
    
    // 缓冲区使用历史
    private List<BufferUsageSnapshot> usageHistory = new ArrayList<>();
    
    // 缓冲区操作统计
    private long totalReads;
    private long totalWrites;
    private long totalAllocations;
    private long totalDeallocations;
    
    // 内存使用情况
    private long usedMemory;
    private long allocatedMemory;
    private double memoryUtilization;
    
    /**
     * 缓冲区使用快照
     */
    @Data
    public static class BufferUsageSnapshot {
        private LocalDateTime timestamp;
        private int capacity;
        private int readableBytes;
        private int writableBytes;
        private double utilization;
        
        public BufferUsageSnapshot(int capacity, int readableBytes, int writableBytes) {
            this.timestamp = LocalDateTime.now();
            this.capacity = capacity;
            this.readableBytes = readableBytes;
            this.writableBytes = writableBytes;
            this.utilization = capacity > 0 ? (double) (capacity - writableBytes) / capacity * 100 : 0;
        }
    }
    
    /**
     * 添加使用快照
     */
    public void addUsageSnapshot() {
        usageHistory.add(new BufferUsageSnapshot(capacity, readableBytes, writableBytes));
        // 只保留最近50个快照
        if (usageHistory.size() > 50) {
            usageHistory.remove(0);
        }
    }
    
    /**
     * 计算内存利用率
     */
    public void calculateMemoryUtilization() {
        if (capacity > 0) {
            this.memoryUtilization = (double) (capacity - writableBytes) / capacity * 100;
        } else {
            this.memoryUtilization = 0;
        }
    }
}