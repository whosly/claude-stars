package com.whosly.stars.java.reactor.performance.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 普通的串行请求方式
 *
 * 所有任务已经执行完毕运行的时长为(ms)：2053.0
 */
public class NormalRequestManagerServiceTest {
    private static final int ROUND = 200;

    private IBizService bizService;

    @BeforeEach
    public void setup() {
        this.bizService = new BizServiceImpl();
    }

    @Test
    public void testNormalRequest() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < ROUND; i++) {
            long tid = Thread.currentThread().getId();
            try {
                this.bizService.doRpc(tid);
                System.out.println("线程" + tid + "的业务操作结束了");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        //输出统计结果
        float time = System.currentTimeMillis() - start;

        System.out.println("所有任务已经执行完毕");
        System.out.println("运行的时长为(ms)：" + time);
    }

}
