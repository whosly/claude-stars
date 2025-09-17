package com.yueny.stars.netty.capacity.alloc;

/**
 * 内存分配器类型
 *
 * @author fengyang
 * @date 2023/8/15 下午4:26
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public enum AllocatorType
{
    /**
     * 堆外
     */
    DIRECT,

    /**
     * 堆内
     */
    HEAP,

    /**
     * 堆外 OR 堆内
     */
    ALL;

}
