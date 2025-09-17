package com.yueny.stars.netty.capacity;

import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author fengyang
 * @date 2023/8/14 下午5:02
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ThreadPools
{
    /**
     * 创建ScheduledExecutorService
     *
     * 核心线程数为1， 最大线程数为 Integer.MAX_VALUE, keepAliveTime 0 的无限 DelayedWorkQueue 队列
     */
    public static ScheduledExecutorService createSingleScheduledExecutor(String namePrefix, Logger log) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

        return scheduledExecutorService;
    }
}
