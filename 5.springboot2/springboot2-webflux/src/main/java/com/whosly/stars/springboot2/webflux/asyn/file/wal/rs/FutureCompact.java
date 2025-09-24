package com.whosly.stars.springboot2.webflux.asyn.file.wal.rs;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.handler.IFileHandlerOpera;
import lombok.*;

import java.util.concurrent.CompletableFuture;

@ToString
@Getter
@Builder
public class FutureCompact {

    // ----------------------------------------
    //                    head
    // ----------------------------------------
    private Long lsnSeq;


    // ----------------------------------------
    //                    body
    // ----------------------------------------
    private WALBlockBody body;

    /**
     * 操作的文件句柄
     */
    @NonNull
    private IFileHandlerOpera fileHandler;

    @Setter
    private CompletableFuture<WALBlockResult> future;

}
