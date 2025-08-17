package com.yueny.study.netty.capacity.allocator;

import com.yueny.study.netty.capacity.alloc.AllocatorType;
import com.yueny.study.netty.capacity.alloc.IAllocator;
import com.yueny.study.netty.capacity.buffer.WrappedAutoFlushByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocatorMetric;

import java.util.Map;

/**
 * 堆外内存分配管理器
 *
 * @author fengyang
 * @date 2023/8/14 下午4:37
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public interface IDirectByteBufPooledAllocator
        extends IAllocator
{
    default AllocatorType type()
    {
        return AllocatorType.DIRECT;
    }

    /**
     * 分配器别名， 用于配置相关的 key 的读取和加载
     */
    String alias();

    int cacheSize();

    /**
     * 分配器的统计信息
     */
    ByteBufAllocatorMetric allocatorMetric();


    /**
     * 分配 [堆外] buffer
     *
     * @return
     */
    WrappedAutoFlushByteBuf allocByteBuf();

    /**
     * 分配 [堆外] buffer
     *
     * @param initialCapacity 初始容量
     * @return
     */
    WrappedAutoFlushByteBuf allocByteBuf(int initialCapacity);

    /**
     * 分配 [堆外] buffer
     *
     * @param maxCapacity 最大容量
     * @return
     */
    WrappedAutoFlushByteBuf allocByteBuf(Long maxCapacity);

    /**
     * 分配 [堆外] buffer
     *
     * @param initialCapacity 初始容量
     * @param maxCapacity 最大容量
     * @return
     */
    WrappedAutoFlushByteBuf allocByteBuf(int initialCapacity, Long maxCapacity);

    /**
     * 分配 [堆外] buffer
     *
     * @param initialCapacity 初始容量
     * @param maxCapacity 最大容量
     * @param scaleDown buffer 是记否允许缩容
     * @return
     */
    WrappedAutoFlushByteBuf allocByteBuf(int initialCapacity, Long maxCapacity, boolean scaleDown);

    /**
     * 销毁 ByteBuf
     */
    void release(ByteBuf byteBuf);

    /**
     * 容量缩容， 缩容至默认的初始大小
     *
     */
    void resize(ByteBuf byteBuf);

    /**
     * 得到 ByteBuf 中的每个连接分配的 buffer 的统计信息
     *
     * key is connIdIBufAllocatorManager
     */
    Map<Long, ByteBufStaticInfo> getByteBufStaticInfo();

    /**
     * 当前分配器缓存分配的所有的堆外内存总大小。单位 byte
     */
    long getUsedDirectMemory();
}
