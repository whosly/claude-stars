package com.whosly.stars.springboot2.webflux.asyn.file.wal.util;

import java.text.SimpleDateFormat;

public enum DateFormatType {
    /**
     * length  17
     */
    FORMAT_MS {
        @Override
        SimpleDateFormat getFormat() {
            return new SimpleDateFormat("yyyyMMddHHmmssSSS");
        }
    },

    /**
     * length  14:  2022_1231_235959
     */
    FORMAT_S{
        @Override
        SimpleDateFormat getFormat() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    },

    /**
     * length  12:  22_1231_235959
     */
    SHORT_YEAR_FORMAT_S{
        @Override
        SimpleDateFormat getFormat() {
            return new SimpleDateFormat("yyMMddHHmmss");
        }
    },

    ;

    abstract SimpleDateFormat getFormat();

}
