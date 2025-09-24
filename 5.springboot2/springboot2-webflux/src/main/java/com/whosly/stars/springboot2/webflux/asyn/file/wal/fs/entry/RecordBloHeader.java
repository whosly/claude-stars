package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatType;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatUtil;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.SumUtil;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesBuffers;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * 固定长度 33
 */
@ToString
@Getter
public class RecordBloHeader implements IByte<RecordBloHeader> {
    private final String crc;

    /**
     * 1个字节， 1字节（byte）占8位（bit），2byte占16bit，4byte占32bit，8byte就是64bit
     *
     * 1字节的取值范围：最大值是：（2^7）-1=127；二进制表示为：01111111
     *              最小值是：-2^7=-128；二进制表示为：10000000
     */
    private final byte type;

    /**
     * 日志序列号，是一个一直递增的整形数字, 表示事务写入到日志的字节总量。
     *
     * 8个字节 long int
     */
    private final Long lsn;

    /**
     * 2个字节  short, char
     * 2字节的取值范围：最大值是：（2^15）-1=32767；二进制表示为：01……1//0后面15个1
     *              最小值是：-2^15=-32768；二进制表示为：10……0//1后面31个0
     *
     * 4个字节 int
     * 4字节的取值范围：最大值是：（2^31）-1=2147483647；二进制表示为：01……1//0后面31个1
     *              最小值是：-2^31=-2147483648；二进制表示为：10……0//1后面63个0
     */
    private final int length;

    // 固定长度 14
    private final String writeAt;

    public RecordBloHeader(RecordHeaderData headerData, byte[] bodyBytes) {
        this(headerData.getType(), headerData.getLsn(), bodyBytes);
    }

    public RecordBloHeader(byte type, Long lsn, byte[] bodyBytes) {
        this.type = type;
        this.lsn = lsn;
        this.length = bodyBytes.length;
        this.writeAt = DateFormatUtil.format(DateFormatType.FORMAT_S);

        byte[] crcBytes = getCrcBytes(type, lsn, length, writeAt, bodyBytes);
        String crc = CRC4.getCRC(crcBytes);
        this.crc = crc;
    }

    private RecordBloHeader(String crc,
                            byte type, Long lsn, int length, String writeAt) {
        this.type = type;
        this.lsn = lsn;
        this.length = length;
        this.writeAt = writeAt;

        this.crc = crc;
    }

    @Override
    public byte[] toBytes() {
        // 固定长度 31 + 2(crc len and writeAt len)
        int capacity = getSum(this.crc.length() + 1, 1, 8, 4, writeAt.length() + 1);

        BytesBuffers instance = BytesBuffers.build(capacity, false);
        instance.writeWithLength(this.crc);
        instance.put(this.type);
        instance.writeLong(lsn);
        instance.writeInt(length);
        instance.writeWithLength(writeAt);

        return instance.array();
    }

    /**
     * bytes 转 IByte bean
     */
    public static final RecordBloHeader fromBytes(byte[] bytes) {
        BytesMessage mm = new BytesMessage(bytes);

        return fromBytes(mm);
    }

    /**
     * bytes 转 IByte bean
     */
    public static final RecordBloHeader fromBytes(BytesMessage mm) {
        int position = mm.position();

        String crc = mm.readStringWithLength();
        byte type = mm.read();
        Long lsn = mm.readLong();
        Integer bodyLength = mm.readInt();
        String writeAt = mm.readStringWithLength();

        return new RecordBloHeader(crc, type, lsn, bodyLength, writeAt);
    }

    private static final byte[] getCrcBytes(byte type, long lsn, int bodyLen, String writeAt, byte[] bodyBytes){
        // 计算 crc,  byte type 1, lsn 8, body-length length, writeAt
        int capacity = SumUtil.getSum(1, 8, 4, writeAt.length() + 1, bodyBytes.length);
        BytesBuffers instance = BytesBuffers.build(capacity);
        instance.put(type);
        instance.writeLong(lsn);
        instance.writeInt(bodyLen);
        instance.writeWithLength(writeAt);
        instance.put(bodyBytes);

        byte[] crcBytes = instance.array();

        return crcBytes;
    }
}
