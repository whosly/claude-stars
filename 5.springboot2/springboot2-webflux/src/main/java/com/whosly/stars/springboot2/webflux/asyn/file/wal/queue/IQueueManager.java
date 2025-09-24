package com.whosly.stars.springboot2.webflux.asyn.file.wal.queue;

import java.util.concurrent.BlockingQueue;

/**
 * 队列管理器
 */
public interface IQueueManager<T> {

    BlockingQueue<T> get();

    /**
     * 放入队列
     */
    boolean offer(T t);

    /**
     * 无限等待，取出队列
     *
     * 当队列为null的时候，take()方法会一直等待，因此会抛出一个InterruptedException类型的异常。
     *
     * poll()方法会直接返回null, 不会抛出异常
     */
    T take() throws InterruptedException;

    void close();

}
