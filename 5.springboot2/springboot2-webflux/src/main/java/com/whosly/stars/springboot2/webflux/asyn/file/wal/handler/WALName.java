package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatType;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * wal 文件名生成器
 */
class WALName implements IWALName {
    private static Logger log = LoggerFactory.getLogger(WALName.class);

    // key : tableId,   value : AtomicLong
    private static final Map<Long, AtomicLong> A = new HashMap<>();

    private static final AtomicLong getAtomic(Long tableId){
        if(!A.containsKey(tableId)){
            synchronized (WALName.class){
                if(!A.containsKey(tableId)){
                    A.put(tableId, new AtomicLong(0));
                }
            }
        }

        return A.get(tableId);
    }

    /**
     * 长度 3 + 1 + 14 + 1 + 2 + SUFFIX.len
     *
     * wal-2022_1101_145020-00
     */
    public static String getName(Long tableId) {
        try {
            long seqId = getAtomic(tableId).getAndIncrement();

            return PREFIX + format(seqId) + SUFFIX;
        } catch (Throwable t) {
            t.printStackTrace();

            return PREFIX + DateFormatUtil.format(DateFormatType.FORMAT_S)
                    + String.format("-%02d", ThreadLocalRandom.current().nextInt(100))
                    + SUFFIX;
        }
    }

    private static String format(long seqId) {
        return DateFormatUtil.format(DateFormatType.FORMAT_S)
                + String.format("-%02d", seqId % 100);
    }

}
