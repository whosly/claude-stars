package com.yueny.study.netty.allocator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 每个 buffer 的统计信息
 *
 * @author fengyang
 * @date 2023/8/14 下午4:58
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
@Builder
@Getter
@ToString
public class ByteBufStaticInfo
{
    /**
     *  the number of bytes (octets) this buffer can contain.
     */
    private int capacity;

    /**
     *  the maximum allowed capacity of this buffer
     */
    private int maxCapacity;

    /**
     * the writerIndex of this buffer.
     */
    private int writerIndex;

    /**
     * Returns the readerIndex of this buffer.
     */
    private int readerIndex;

    /**
     * Returns the number of writable bytes which is equal to (this.capacity - this.writerIndex).
     */
    private int writableBytes;

    /**
     * Returns the number of readable bytes which is equal to (this.writerIndex - this.readerIndex).
     */
    private int readableBytes;

    /**
     * Returns the maximum possible number of writable bytes, which is equal to (this.maxCapacity - this.writerIndex).
     */
    private int maxWritableBytes;

    /**
     * is Direct
     */
    private boolean isDirect;
}
