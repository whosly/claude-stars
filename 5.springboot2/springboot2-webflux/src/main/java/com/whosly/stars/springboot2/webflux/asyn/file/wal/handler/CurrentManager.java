package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CurrentManager {
    private final Long databaseId;
    private final Long tableId;

    public CurrentManager(Long databaseId, Long tableId) {
        this.databaseId = databaseId;
        this.tableId = tableId;
    }

    /**
     * close
     */
    public void close(){
        //.
    }

    /**
     * CURRENT 文件路径
     */
    public String getCurrentFilePath(){
        return WALCompatibilityAction.getDirectoryAbsolutePath(databaseId, tableId)
                + File.separator + "CURRENT";
    }

    /**
     * current 文件是否存在
     * @return
     */
    public boolean isCurrentFileExist(){
        boolean isCurrentExist = FileUtil.exist(
                WALCompatibilityAction.getFile(databaseId, tableId, "CURRENT"));

        return isCurrentExist;
    }

    public boolean writeContext(String context) throws IOException {
        String currentFilePath = getCurrentFilePath();
        FileUtils.writeStringToFile(new File(currentFilePath), context);

        return true;
    }
}
