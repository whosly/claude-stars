package com.yueny.study.netty.capacity.alloc;

/**
 * 标志， 用于标志当前为(堆外、堆内)内存分配器，便于后续整合与管理
 *
 * @author fengyang
 * @date 2023/8/15 下午4:24
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public interface IAllocator
{
    /**
     * 内存分配器名称
     */
    String getName();

    /**
     * 内存分配器类型
     */
    AllocatorType type();
}
