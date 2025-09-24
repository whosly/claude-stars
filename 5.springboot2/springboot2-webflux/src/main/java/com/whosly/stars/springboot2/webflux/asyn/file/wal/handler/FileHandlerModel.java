package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class FileHandlerModel {

    /**
     * 是否为刚创建的文件,  false 为未创建
     */
    @Setter
    private boolean openAndCreate;

    /**
     * open 时文件大小
     */
    private long openSize;

    /**
     * 句柄的实际文件名
     */
    private String finalFileName;
}
