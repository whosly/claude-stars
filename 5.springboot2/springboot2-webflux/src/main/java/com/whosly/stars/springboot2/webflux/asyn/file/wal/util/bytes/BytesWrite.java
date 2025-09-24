package com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BytesWrite {
    private static final Logger logger = LoggerFactory.getLogger(BytesWrite.class);

    private byte[] data;

    private int length = 0;

    private int position;
    private boolean noEx = true;

    public BytesWrite(byte[] buf) {
        data = new byte[buf.length];
        System.arraycopy(buf, 0, data, 0, buf.length);
        setLength(data.length);
        position = 0;
    }

    public BytesWrite(int size) {
        this(size, true);
    }

    /**
     *
     * @param size
     * @param noEx 是否自动扩容。  true 是
     */
    public BytesWrite(int size, boolean noEx) {
        data = new byte[size];
        setLength(data.length);
        position = 0;
        this.noEx = noEx;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if (this.position < position) {
            int length = position - this.position;
            ensureCapacity(length);
        }
        this.position = position;
    }

    /**
     * 读当前 position 位置的字节
     */
    public byte readByte() {
        return data[position++];
    }

    /**
     * 读指定位置的字节
     */
    public byte readByte(int position) {
        this.position = position;
        return data[this.position++];
    }

    @Deprecated
    public int readBytes(byte[] ab, int offset, int len) {
        System.arraycopy(data, position, ab, offset, len);
        position += len;
        return len;
    }

    public void writeByte(byte b) {
        ensureCapacity(1);
        data[position++] = b;
    }

    public int writeBytes(byte[] ab) {
        return writeBytes(ab, 0, ab.length);
    }

    private int writeBytes(byte[] ab, int offset, int len) {
        ensureCapacity(len);
        System.arraycopy(ab, offset, data, position, len);
        position += len;
        return len;
    }

    /**
     * 可能需要扩容的增加的buffer长度
     *
     * @param ensureCapacityLen  需要新分配的空间大小
     *
     * @return   新增的空间大小
     */
    protected int ensureCapacity(int ensureCapacityLen) {
        logger.trace("try ensureCapacity, noEx:{}, current position:{}, " +
                        "data.length:{}, getLength():{}, " +
                        "ensureCapacity len:{}",
                this.noEx, position,
                data.length,  getLength(),
                ensureCapacityLen);

        /* position 与 新分配空间的和，小于现有的总大小，则需要扩容. 否则不需要扩容 */
        if ((position + ensureCapacityLen) <= getLength()) {
            return 0;
        }

//        if(!this.noEx){
//            return 0;
//        }

        /* 剩余空间不够，需要扩容 */
        if ((position + ensureCapacityLen) < data.length) {
            // 如果是小于数组的昌吉长度，其实也不需要在分配
            setLength(data.length);
            return 0;
        }

        // 数组赋值好耗时， 因此扩容 * 1.5
        int newLength = (int) (position + ensureCapacityLen * 1.5);

        if (newLength <= (data.length + ensureCapacityLen)) {
            newLength = (data.length + ensureCapacityLen + 1) + (int) (ensureCapacityLen * 1.25);
        }

        logger.trace("try ensureCapacity, newLength:{}", newLength);

        byte[] newBytes = new byte[newLength];
        System.arraycopy(data, 0, newBytes, 0, data.length);
        data = newBytes;
        setLength(data.length);

        return ensureCapacityLen;
    }

    /**
     * 重置
     */
    public synchronized void reset() {
        this.position = 0;
        setLength(data.length);
    }

    /**
     * 剩余可用大小， limit - position
     *
     * @return
     */
    public int remaining() {
        return this.length - this.position;
    }

    /**
     * 是否还与可用空间
     *
     * @return true 存在可用空间
     */
    public boolean hasRemaining() {
        return (remaining() > 0);
    }

    /**
     * 跳过
     *
     * @param bytes 跳过的字节数
     */
    public void skip(int bytes) {
        this.position += bytes;
    }

    public byte[] array() {
        return data;
    }

    /**
     * 数组拷贝
     */


    /**
     * 可能需要扩容的增加的buffer长度, 拷贝 source的数据范围： [start,  end)
     *
     * @param newBytesLength  拷贝的新数组的大小
     * @param source  数据源
     * @param start  开始的位置， 包括
     * @param end  结束的位置，不包括
     *
     * @return  拷贝后的数据
     */
    public static final byte[] copy(int newBytesLength, byte[] source, int start, int end) {
        byte[] newBytes = new byte[newBytesLength];

        System.arraycopy(source, 0, newBytes, 0, end);

        return newBytes;
    }

}
