package com.whosly.stars.java.reactor.performance.optimization;

public class BizServiceImpl implements IBizService {

    @Override
    public void doRpc(long tid) throws RuntimeException {
        System.out.println(tid + "开始进行业务操作,模拟远程调用, sleep 200ms");

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
