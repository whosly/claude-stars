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
 * 响应式 Flux 流进行异步回调。
 *
 * 分析一下：执行一次WEB调用操作所需的实际工作非常少。实际的工作就2点：1. 生成并触发一个请求  2. 等等等待200ms(低性能环节)，会收到一个响应。
 *
 * 线程资源是宝贵的。应用线程一般最多就400个左右。如果开启太多，很多的cpu资源就用去做线程上下文切换了。
 *
 * 如何要让这些线程不等待，去干别的工作，该当如何？答案就是使用异步框架。使用异步框架发出 Web 请求的核心优势之一，在请求进行中时您不会占用任何线程。
 * 因此选择 响应式 Flux 流进行异步回调改造。
 *
 * 所有任务已经执行完毕：并发执行的时间(ms): 2788
 */
public class FluxManagerServiceTest {
    private static final int ROUND = 200;

    private IBizService bizService;

    @BeforeEach
    public void setup() {
        this.bizService = new BizServiceImpl();
    }

    @Test
    public void testFlux() throws InterruptedException {
        long start = System.currentTimeMillis();

        List<Integer> targetKeys = new ArrayList<>();
        for (int i = 0; i < ROUND; i++) {
            targetKeys.add(i);
        }

        // 使用 CountDownLatch 确保异步操作完成
        CountDownLatch latch = new CountDownLatch(1);

        Flux.fromIterable(targetKeys)
                .subscribeOn(Schedulers.boundedElastic())
                // 使用 flatMap 并指定并发数来实现并行处理
                .flatMap(key -> doRpcFlux(key), 16)
                // 指定调度器以实现真正的异步执行
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(FluxManagerServiceTest::doOnError)
                .doOnComplete(FluxManagerServiceTest::doOnComplete)
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

    private Mono<Integer> doRpcFlux(int key) {
        return Mono.fromCallable(() -> {
            this.bizService.doRpc((long) key);
            return key;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}