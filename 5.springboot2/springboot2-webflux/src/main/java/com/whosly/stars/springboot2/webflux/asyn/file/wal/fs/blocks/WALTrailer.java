package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks;

import cn.hutool.core.lang.Assert;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.IByte;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.handler.IWALName;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesBuffers;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesMessage;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 文件尾部信息
 */
@ToString
public class WALTrailer implements IByte<WALTrailer> {
    /**
     * NULL is sent as 0xfb, is -5
     */
    public static final byte NULL_MARK = (byte) 0xfb;
    /**
     * NULL is sent as 0xfb
     */
    private static final byte[] DEFAULT_FILLER = new byte[23];

    /**
     * 结束标记
     */
    @Getter
    private final byte flag = NULL_MARK;

    /**
     * 指向下一个文件的文件名
     */
    @Getter
    private final String next;

    /**
     * 预留字段
     */
    private final byte[] filler = DEFAULT_FILLER;

    public WALTrailer(String next) {
        this(NULL_MARK, DEFAULT_FILLER, next);
    }

    private WALTrailer(byte flag, byte[] filters, String next) {
        this.next = next;
    }

    /**
     * IByte bean 转 bytes
     */
    @Override
    public byte[] toBytes() {
        int capacity = 1 + IWALName.nameLength() + 1 + filler.length;

        BytesBuffers instance = BytesBuffers.build(capacity, false);
        instance.put(flag);
        instance.writeWithLength(next);
        instance.put(filler);

        return instance.array();
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALTrailer fromBytes(byte[] bytes) {
        BytesMessage mm = new BytesMessage(bytes);

        return fromBytes(mm);
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALTrailer fromBytes(BytesMessage mm) {
        byte flag = mm.read();
        String nextFileName = mm.readStringWithLength();
        byte[] filters = mm.readBytes(DEFAULT_FILLER.length);

        WALTrailer instance = new WALTrailer(nextFileName);

        Assert.isTrue(instance.flag == flag);
        Assert.isTrue(StringUtils.isNotEmpty(nextFileName));
        Assert.isTrue(instance.filler.length == filters.length);

        return instance;
    }

}
