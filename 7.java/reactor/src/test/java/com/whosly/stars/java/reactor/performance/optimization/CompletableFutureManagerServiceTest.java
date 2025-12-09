package com.whosly.stars.java.reactor.performance.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用CompletableFuture 和  CountDownLatch  进行并发回调。
 *
 * 有两个方法：
 * 方法1：CompletableFuture 和 CountDownLatch
 * 方法2：CompletableFuture.allOf 算子
 *
 * 当前使用了方法1.
 *
 * 所有任务已经执行完毕运行的时长为(ms)：2661.0
 */
public class CompletableFutureManagerServiceTest {
    private static final int ROUND = 200;

    private IBizService bizService;

    @BeforeEach
    public void setup() {
        this.bizService = new BizServiceImpl();
    }

    @Test
    public void testCompletableFuture() {
        CountDownLatch countDownLatch = new CountDownLatch(ROUND);

        //批量异步
        ExecutorService executor = Executors.newFixedThreadPool(16);
        long start = System.currentTimeMillis();
        for (int i = 0; i < ROUND; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long tid = Thread.currentThread().getId();
                try {
                    //异步执行 远程调用
                    this.bizService.doRpc(tid);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return tid;
            }, executor);

            future.thenAccept((tid) -> {
                System.out.println("线程" + tid + "结束了");
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
            //输出统计结果
            float time = System.currentTimeMillis() - start;

            System.out.println("所有任务已经执行完毕");
            System.out.println("运行的时长为(ms)：" + time);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
