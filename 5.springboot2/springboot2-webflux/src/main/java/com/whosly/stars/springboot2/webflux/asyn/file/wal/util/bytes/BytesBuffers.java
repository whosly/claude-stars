package com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字节码写入
 */
public class BytesBuffers {

    /**
     *
     * @param maxCapacity  4 * 1024
     * @return
     */
    public static final BytesBuffers build(int maxCapacity){
        return new BytesBuffers(
                new BytesWrite(maxCapacity)
        );
    }

    /**
     *
     * @param maxCapacity  4 * 1024
     * @param noEx  是否自动扩容。 false 为不自动扩容
     * @return
     */
    public static final BytesBuffers build(int maxCapacity, boolean noEx){
        return new BytesBuffers(
                new BytesWrite(maxCapacity, noEx)
        );
    }


    private BytesWrite write;

    private BytesBuffers(BytesWrite write){
        this.write = write;
    }

    // write byte
    public void put(byte b) {
        write.writeByte(b);
    }

    public void put(byte[] src) {
        int offset = 0;
        int length = src.length;

        if (length > write.remaining()){
            ensureCapacity(length - write.remaining());
        }

        int end = offset + length;
        for (int i = offset; i < end; i++){
            this.put(src[i]);
        }
    }

    // writeInt
    public void writeUB2(int i) {
        put((byte) (i & 0xff));
        put((byte) (i >>> 8));
    }

    // is writeLongInt
    public void writeUB3(int i) {
        put((byte) (i & 0xff));
        put((byte) (i >>> 8));
        put((byte) (i >>> 16));
    }

    public void writeInt(int i) {
        put((byte) (i & 0xff));
        put((byte) (i >>> 8));
        put((byte) (i >>> 16));
        put((byte) (i >>> 24));
    }

    public void writeFloat(float f) {
        writeInt(Float.floatToIntBits(f));
    }

    public void writeUB4(long l) {
        put((byte) (l & 0xff));
        put((byte) (l >>> 8));
        put((byte) (l >>> 16));
        put((byte) (l >>> 24));
    }

    public void writeUB4(byte[] l4) {
        for (int i = 0; i < 4; i++){
            this.put(l4[i]);
        }
    }

    // writeLongLong
    public void writeLong(long l) {
        put((byte) (l & 0xff));
        put((byte) (l >>> 8));
        put((byte) (l >>> 16));
        put((byte) (l >>> 24));
        put((byte) (l >>> 32));
        put((byte) (l >>> 40));
        put((byte) (l >>> 48));
        put((byte) (l >>> 56));
    }

    public void writeDouble(double d) {
        writeLong(Double.doubleToLongBits(d));
    }

    public void writeWithNull(String src, Charset charset) {
        writeWithNull(src.getBytes(charset));
    }

    public void writeWithNull(byte[] src) {
        put(src);
        put((byte) 0);
    }

    public void writeWithLength(String src) {
        if (src != null) {
            byte[] b = src.getBytes(StandardCharsets.UTF_8);

            writeWithLength(b);
        } else {
            // write null flag
            writeWithLength(null, (byte) 251);
        }
    }

    public void writeLength(long length) {
        if (length < 251) {
            put((byte) length);
        } else if (length < 0x10000L) {// 0x10000L --> 65536
            ensureCapacity(1+2);
            put((byte) 252);
            writeUB2((int) length);
        } else if (length < 0x1000000L) {// 0x1000000L --> 16777216
            ensureCapacity(1+3);
            put((byte) 253);
            writeUB3((int) length);
        } else {
            ensureCapacity(1+8);
            put((byte) 254);
            writeLong(length);
        }
    }

    public void writeWithLength(byte[] src) {
        // 最大限制
        int limit = write.getLength();
        // 当前字节位置
        int position = write.getPosition();
        // 剩余可用大小， limit - position
        int remaining = write.remaining();

        int length = src.length;
        writeLength(length);

        put(src);
    }

    public void writeWithLength(byte[] src, byte nullValue) {
        if (src == null) {
            put(nullValue);
        } else {
            writeWithLength(src);
        }
    }

    /**
     * 获取最终数据数据
     */
    public byte[] array() {
        int ps = write.getPosition();

        byte[] source = write.array();

        // 移除空数组
        byte[] newBytes = BytesWrite.copy(ps, source, 0, ps);
        source = null;

        return newBytes;
    }

    /**
     * 扩容
     */
    public int getLength() {
        return write.getLength();
    }

    /**
     * 扩容
     *
     * @param additionalData  扩容的大小
     */
    public int ensureCapacity(int additionalData) {
        return write.ensureCapacity(additionalData);
    }

}
