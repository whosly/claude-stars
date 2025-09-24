package com.whosly.stars.springboot2.webflux.asyn.file.wal;


import java.util.Optional;

/**
 * 文件异步写
 */
public interface IWalFile {

    /**
     * 打开将要写的文件。 此处不真实打开文件，而是实例化一个文件句柄
     *
     * @param tableId tableId
     * @return
     */
    Optional<IFileHandler> open(Long databaseId, Long tableId);

    /**
     * 句文件流关闭, 柄关闭
     */
    boolean close(Long tableId);

}
