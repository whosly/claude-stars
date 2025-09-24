package com.whosly.stars.netty.capacity.allocator;

/**
 * @author fengyang
 * @date 2023/8/14 下午4:55
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class Allocators
{
    ////////////////////////////////////////////////////////
    //                    容量换算单位                      //
    ////////////////////////////////////////////////////////
    public static final int _1K = 1 * 1024;
    public static final int _1MB = _1K * 1024;
    public static final int _512MB = _1MB * 512;
    public static final long _1GB = _1MB * 1024;
    public static final long _2GB = _1GB * 2;
}
