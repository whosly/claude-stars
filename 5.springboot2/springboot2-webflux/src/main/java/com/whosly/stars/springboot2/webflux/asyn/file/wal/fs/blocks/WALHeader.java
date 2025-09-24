package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.IByte;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatType;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatUtil;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesBuffers;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class WALHeader implements IByte<WALHeader> {
    /**
     * NULL is sent as 0xfb
     */
    private static final byte[] DEFAULT_FILLER = new byte[9];

    private final byte type;
    private final byte version;

    /**
     * 日志序列号，是一个一直递增的整形数字, 表示事务写入到日志的字节总量。
     */
    private final Long startLsn;

    /**
     * 指向上一个文件的文件名
     */
    @Getter
    private final String prev;

    /**
     */
    private final int length;

    // 固定长度 14
    private final String createAt;

    /**
     * 预留字段
     */
    private final byte[] filler = DEFAULT_FILLER;

    public WALHeader(byte type, Long startLsn, String prev) {
        this(type, startLsn, prev, 0);
    }

    public WALHeader(byte type, Long startLsn, String prev, int length) {
        this(type, startLsn, prev, length, DateFormatUtil.format(DateFormatType.FORMAT_S));
    }

    private WALHeader(byte type, Long startLsn, String prev, int length, String createAt) {
        this.type = type;
        this.version = (byte) 1;
        this.startLsn = startLsn;
        this.prev = prev;
        this.length = length;
        this.createAt = createAt;
    }

    /**
     * IByte bean 转 bytes
     */
    @Override
    public byte[] toBytes() {
        // 固定长度 ,  type 1, version 1. startLsn  8,  prev + 1, length 4
        int capacity = getSum(1, 1, 8, prev.length() + 1, 4,
                // filler.len, createAt 14 + 1(len)
                filler.length, createAt.length() + 1);

        BytesBuffers instance = BytesBuffers.build(capacity, false);
        instance.put(this.type);
        instance.put(this.version);
        instance.writeLong(startLsn);
        instance.writeWithLength(prev);
        instance.writeInt(length);
        instance.put(filler);
        instance.writeWithLength(createAt);

        return instance.array();
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALHeader fromBytes(byte[] bytes) {
        BytesMessage mm = new BytesMessage(bytes);

        return fromBytes(mm);
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALHeader fromBytes(BytesMessage mm) {
        int position = mm.position();

        byte type = mm.read();
        byte version = mm.read();
        Long startLsn = mm.readLong();
        String prev = mm.readStringWithLength();
        Integer length = mm.readInt();
        byte [] filters = mm.readBytes(DEFAULT_FILLER.length);
        String createAt = mm.readStringWithLength();

        return new WALHeader(type, startLsn, prev, length, createAt);
    }

}
