package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.block;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.IByte;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordBloEntry;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordHeaderData;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordBloHeader;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesBuffers;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesMessage;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class WALBlockLog implements IByte<WALBlockLog> {
    private final RecordBloHeader header;

    private final RecordBloEntry body;
    private final byte[] bodyBytes;

    public WALBlockLog(RecordHeaderData headerData, RecordBloEntry body) {
        this.body = body;
        this.bodyBytes = body.toBytes();

        this.header = new RecordBloHeader(headerData, bodyBytes);
    }

    private WALBlockLog(RecordBloHeader header, RecordBloEntry body) {
        this.body = body;
        this.bodyBytes = body.toBytes();

        this.header = header;
    }

    @Override
    public byte[] toBytes() {
        byte[] headerBytes = header.toBytes();

        int maxCapacity = getSum(headerBytes.length, bodyBytes.length);
        BytesBuffers instance = BytesBuffers.build(maxCapacity, false);

        instance.put(headerBytes);
        instance.put(bodyBytes);

        return instance.array();
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALBlockLog fromBytes(byte[] bytes) {
        BytesMessage mm = new BytesMessage(bytes);

        return fromBytes(mm);
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALBlockLog fromBytes(BytesMessage mm) {
        RecordBloHeader header = RecordBloHeader.fromBytes(mm);
        RecordBloEntry body = RecordBloEntry.fromBytes(mm);

        WALBlockLog log = new WALBlockLog(header, body);

        return log;
    }

}
