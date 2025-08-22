package com.yueny.stars.netty.capacity.allocator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author fengyang
 * @date 2023/8/14 下午4:17
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
@ToString
@Getter
@Builder
class ByteBufPoolConfig
{
    private Long maxCapacity;

    private Long maxDirectMemory;
}
