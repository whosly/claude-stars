package com.yueny.study.netty.allocator;

import com.yueny.study.netty.allocator.BytesUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengyang
 * @date 2023/8/10 下午3:41
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
@Slf4j
public class AppTest
{
    private static final int _1K = 1 * 1024;
    private static final int _1MB = _1K * 1024;
    private static final long _1GB = _1MB * 1024;

    public static void main(String[] args)
    {
        log.info("4 * _1K << 5 :{}", BytesUtil.byteToM(4 * _1K << 5));
        log.info("4 * _1K << 6 :{}", BytesUtil.byteToM(4 * _1K << 6));
        log.info("4 * _1K << 7 :{}", BytesUtil.byteToM(4 * _1K << 7));
        log.info("4 * _1K << 8 :{}", BytesUtil.byteToM(4 * _1K << 8));
        log.info("4 * _1K << 9 :{}", BytesUtil.byteToM(4 * _1K << 9));
        log.info("4 * _1K << 10 :{}", BytesUtil.byteToM(4 * _1K << 10));
        log.info("4 * _1K << 11 :{}", BytesUtil.byteToM(4 * _1K << 11));
        log.info("4 * _1K << 12 :{}", BytesUtil.byteToM(4 * _1K << 12));

        // test
        log.info("get :{}", BytesUtil.byteToM(8 * 6 * (4 * _1K << 7)));

    }
}
