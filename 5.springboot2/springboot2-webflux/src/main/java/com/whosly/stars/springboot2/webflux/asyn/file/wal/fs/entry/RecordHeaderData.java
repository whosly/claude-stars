package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class RecordHeaderData {
    private final byte type;

    private final Long lsn;

    public RecordHeaderData(Integer type, Long lsn) {
        this.type = type.byteValue();
        this.lsn = lsn;
    }

}
