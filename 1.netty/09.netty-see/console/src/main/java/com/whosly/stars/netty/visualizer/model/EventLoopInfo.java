package com.whosly.stars.netty.visualizer.model;

import lombok.Data;
import java.util.List;

/**
 * EventLoop信息模型
 * 
 * @author fengyang
 */
@Data
public class EventLoopInfo {
    private String name;
    private String type;
    private int threadCount;
    private List<String> channels;
    private long tasksExecuted;
    private long tasksQueued;
    private boolean shutdown;
    private double cpuUsage;
}