package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class FileSchmaData {
    @Getter
    private final Long databaseId;

    @Getter
    private final Long tableId;

    /**
     * 数据文件名。 可能不是最终文件
     */
    @Getter
    private final String sourceFileName;

}
