package com.whosly.stars.springboot2.webflux.asyn.file.wal;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.handler.FileHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalFileImpl implements IWalFile {

    private static final Logger logger = LoggerFactory.getLogger(WalFileImpl.class);

    @Override
    public Optional<IFileHandler> open(Long databaseId, Long tableId) {
        // 此处得到一个文件句柄。 多次open的时候，拿到的句柄可能相同，可能因为文件满而产生新句柄
        Optional<IFileHandler> fileHandler = FileHandlerManager.tryGet(databaseId, tableId);

        return fileHandler;
    }

    /**
     * 句文件流关闭, 柄关闭
     *
     * @param tableId
     */
    @Override
    public boolean close(Long tableId) {
        FileHandlerManager.close(tableId);

        return false;
    }

}
