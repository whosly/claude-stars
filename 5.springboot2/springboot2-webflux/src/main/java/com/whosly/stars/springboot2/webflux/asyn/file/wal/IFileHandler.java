package com.whosly.stars.springboot2.webflux.asyn.file.wal;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;

import java.util.concurrent.CompletableFuture;

public interface IFileHandler {

    String getFileName();

    /**
     * 提交数据。 数据放入队列。
     *
     * 队列消费后， 返回future
     *
     * @return
     */
    CompletableFuture<WALBlockResult> commit(WALBlockBody reqData);

    /**
     * 当前耗时
     */
    Long getIOCostTime();


    // ----------------------------------------------------
    //                   read
    // ----------------------------------------------------



}
