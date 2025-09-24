package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

public class RecordHeaderBuilder {
    public static final RecordHeaderData build(Integer type, Long lsn){
        return new RecordHeaderData(type, lsn);
    }
}
