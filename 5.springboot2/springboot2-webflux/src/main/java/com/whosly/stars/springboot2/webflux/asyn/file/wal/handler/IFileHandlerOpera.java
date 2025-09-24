package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.IFileHandler;

import java.io.IOException;

public interface IFileHandlerOpera extends IFileHandler {

    // ----------------------------------------------------
    //                   write
    // ----------------------------------------------------
    /**
     *
     * @param val 追加的内容
     *
     * @return lsn seq
     */
    Long appendFile(byte[] val) throws IOException;


    // ----------------------------------------------------
    //                   manager
    // ----------------------------------------------------
    /**
     * 句柄自我检查
     *
     * @return false 检查不通过
     */
    boolean check();

    /**
     * 句柄关闭，文件流关闭
     */
    boolean close();

}
