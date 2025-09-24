package com.whosly.stars.java.delay;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 为啥不用  Thread.sleep？
 *
 * 不行… 如果你在回调里直接  sleep，你就把执行回调的线程卡住了，线程池容易被占满。
 *
 * ScheduledExecutorService  是更优的，定时触发、线程可复用，还能统一管理收尾。再一个，异常别丢了，用  completeExceptionally  原样传出去，这样上游超时、网络错，都能“晚点原样报”。
 * 
 * 
 * 用   thenCompose   加一个“延时用的占位 Future”。也行，先弄个   CompletableFuture<Void>，用 scheduler 在 N 毫秒后   complete(null)，
 * 然后   src.handle(...).thenCompose(_ -> delayFuture)，最后把结果再   thenApply   回来。
 * 
 * @author fengyang
 * @date 2025-09-24 17:11:30
 * @description
 */
public class DelaySomething {
    private static final ScheduledExecutorService SCHED =
            Executors.newScheduledThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

    // 核心：把一个 Future 的完成“延迟”暴露
    public static <T> CompletableFuture<T> delayResolve(CompletableFuture<T> src, long delayMs) {
        CompletableFuture<T> out = new CompletableFuture<>();
        src.whenComplete((v, ex) -> {
            SCHED.schedule(() -> {
                if (ex != null) {
                    out.completeExceptionally(ex);
                } else {
                    out.complete(v);
                }
            },
                    // 也可以随机抖动。 ThreadLocalRandom.current().nextLong(min,max)
                    Math.max(0, delayMs),
                    TimeUnit.MILLISECONDS);
        });
        return out;
    }

    // 批量：按索引阶梯式延迟，比如第 i 个多等 i*step
    public static <T> List<CompletableFuture<T>> delayEach(List<CompletableFuture<T>> list, long baseMs, long stepMs) {
        return IntStream.range(0, list.size())
                .mapToObj(i -> delayResolve(list.get(i), baseMs + i * stepMs))
                .collect(Collectors.toList());
    }

    // 小演示
    public static void main(String[] args) {
        // 模拟三条异步：100ms、200ms、50ms完成
        List<CompletableFuture<Integer>> raw = java.util.List.of(
                            java.util.concurrent.CompletableFuture.supplyAsync(() -> sleepAnd(100, 1)),
                            java.util.concurrent.CompletableFuture.supplyAsync(() -> sleepAnd(200, 2)),
                            java.util.concurrent.CompletableFuture.supplyAsync(() -> sleepAnd(50, 3))
                        );

        // 统一延迟 300ms 再“对外完成”
        List<CompletableFuture<Integer>> delayed = raw.stream()
                .map(f -> delayResolve(f, 300))
                .collect(Collectors.toList());

        // 或者阶梯：基础 100ms，逐个加 100ms
        // List<CompletableFuture<Integer>> delayed = delayEach(raw, 100, 100);

        long t0 = System.currentTimeMillis();
        delayed.forEach(f -> f.thenAccept(v ->
                                System.out.println("visible@" + (System.currentTimeMillis() - t0) + "ms -> " + v)));

        // 别让主线程过早退出
        java.util.concurrent.CompletableFuture.allOf(delayed.toArray(new CompletableFuture[0])).join();
        SCHED.shutdown();
    }

    private static int sleepAnd(long ms, int val) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
        return val;
    }
}
