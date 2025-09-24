package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

public interface IWALName {
    /**
     * 前缀
     */
    String PREFIX = "wal-";

    /**
     * 后缀
     */
    public static final String SUFFIX = "";

    String NAME_FORMAT_EG = PREFIX + "yyyyMMddHHmmss-dd" + SUFFIX;

    static int nameLength(){
        return NAME_FORMAT_EG.length();
    }
}
