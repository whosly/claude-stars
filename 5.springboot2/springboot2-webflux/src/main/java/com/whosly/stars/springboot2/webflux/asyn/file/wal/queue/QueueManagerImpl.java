package com.whosly.stars.springboot2.webflux.asyn.file.wal.queue;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.rs.FutureCompact;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 队列管理器
 */
public class QueueManagerImpl implements IQueueManager<FutureCompact> {
    private static final int QUEUE_SIZE = 100;
    private static final BlockingQueue<FutureCompact> activeQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    public BlockingQueue<FutureCompact> get() {
        return activeQueue;
    }

    @Override
    public boolean offer(FutureCompact futureCompact) {
        return activeQueue.offer(futureCompact);
    }

    @Override
    public FutureCompact take() throws InterruptedException {
        return activeQueue.take();
    }

    @Override
    public void close() {
        //
    }
}
