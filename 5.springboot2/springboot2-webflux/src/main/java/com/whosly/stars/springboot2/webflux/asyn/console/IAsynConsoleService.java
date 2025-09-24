package com.whosly.stars.springboot2.webflux.asyn.console;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;

import java.util.concurrent.CompletableFuture;

/**
 * 异步服务
 *
 */
public interface IAsynConsoleService {

    /**
     * 提交数据。 数据放入队列。
     *
     * 队列消费后， 返回future
     *
     * @return
     */
    CompletableFuture<WALBlockResult> submit(WALBlockBody reqData);

}
