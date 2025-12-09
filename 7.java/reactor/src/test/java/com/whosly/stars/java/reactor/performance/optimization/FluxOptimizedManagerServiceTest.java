package com.whosly.stars.java.reactor.performance.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * 响应式 parallel Flux 流进行并行异步回调
 *
 * flux的内部机制：flux流里边的任务，默认是串行执行的。需要使用 Flux parallel流。
 *
 * 所有任务已经执行完毕：并发执行的时间(ms): 2780
 */
public class FluxOptimizedManagerServiceTest {
    private static final int ROUND = 200;

    private IBizService bizService;

    @BeforeEach
    public void setup() {
        this.bizService = new BizServiceImpl();
    }

    @Test
    public void testFluxOptimized() throws InterruptedException {
        long start = System.currentTimeMillis();

        List<Integer> targetKeys = new ArrayList<>();
        for (int i = 0; i < ROUND; i++) {
            targetKeys.add(i);
        }

        // 使用 CountDownLatch 确保异步操作完成
        CountDownLatch latch = new CountDownLatch(1);

        Flux.fromIterable(targetKeys)
                // 使用 parallel 调度器实现真正的并行处理
                .parallel(16)
                .runOn(Schedulers.boundedElastic())
//                .runOn(Schedulers.parallel())
                // 使用 map 替代 flatMap，因为 doRpc 是同步方法
                .map(key -> {
                    this.bizService.doRpc((long) key);
                    return key;
                })
                .sequential()
                .doOnError(FluxOptimizedManagerServiceTest::doOnError)
                .doOnComplete(FluxOptimizedManagerServiceTest::doOnComplete)
                .doFinally(signalType -> {
                    System.out.println("并发执行的时间(ms): " + (System.currentTimeMillis() - start));
                    latch.countDown(); // 异步操作完成，释放锁存器
                })
                .subscribe(responseData -> {
                    // 空的 onNext 处理
                }, e -> {
                    System.err.println("error: " + (System.currentTimeMillis() - start));
                    latch.countDown(); // 出错时也要释放锁存器
                });

        // 等待异步操作完成
        latch.await();
    }

    private static void doOnError(Throwable throwable) {
        Objects.requireNonNull(throwable, "onError");

        throwable.printStackTrace();
    }

    private static void doOnComplete() {
        System.out.println("并发远程调用异常完成");
    }
}
