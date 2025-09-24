package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesBuffers;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * 数据体
 */
@ToString
@Getter
public class RecordBloEntry implements IByte<RecordBloEntry> {
    private String processId;

    private Long tid;

    private String sql;

    public RecordBloEntry(String processId, Long tid, String sql) {
        this.processId = processId;
        this.tid = tid;
        this.sql = sql;
    }

    @Override
    public byte[] toBytes() {
        byte[] processIdByte = getBytes(this.processId);
        byte[] tidByte = getBytes((this.tid == null ? 0 : this.tid)  + "");
        byte[] sqlByte = getBytes(this.sql);

        int processIdByteLen = processIdByte.length;
        int tidByteLen = tidByte.length;
        int sqlByteLen = sqlByte.length;

        // 计算最大可分配空间
        int maxCapacity = getSum(3, processIdByteLen, tidByteLen, sqlByteLen);
        BytesBuffers instance = BytesBuffers.build(maxCapacity);

        instance.writeWithLength(processIdByte);
        instance.writeWithLength(tidByte);
        instance.writeWithLength(sqlByte);

        return instance.array();
    }

    /**
     * bytes 转 IByte bean
     */
    public static final RecordBloEntry fromBytes(byte[] bytes) {
        BytesMessage mm = new BytesMessage(bytes);

        return fromBytes(mm);
    }

    /**
     * bytes 转 IByte bean
     */
    public static final RecordBloEntry fromBytes(BytesMessage mm) {
        int position = mm.position();

        String processId = mm.readStringWithLength();

        String tidValue = mm.readStringWithLength();
        Long tid = Long.parseLong(tidValue);

        String sql = mm.readStringWithLength();

        return new RecordBloEntry(processId, tid, sql);
    }

}
