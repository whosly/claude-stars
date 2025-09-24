package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatType;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.DateFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

class LSNId {
    private static Logger log = LoggerFactory.getLogger(LSNId.class);
    private static final AtomicLong A = new AtomicLong(0);

    /**
     * Long
     *
     * 22_1231_235959_666666L
     */
    public static Long getN() {
        Long lsn_ = 22_1231_235959_9999999L;
        String lsn = get();

        return Long.parseLong(lsn);
    }

    /**
     * 长度  12 + 7
     *
     * 22_1231_235959_9999999L
     */
    public static String get() {
        try {
            long seqId = A.getAndIncrement();

            return format(seqId);
        } catch (Throwable t) {
            t.printStackTrace();

            return DateFormatUtil.format(DateFormatType.SHORT_YEAR_FORMAT_S)
                    + String.format("%07d", ThreadLocalRandom.current().nextInt(10000000));
        }
    }

    private static String format(long seqId) {
        return DateFormatUtil.format(DateFormatType.SHORT_YEAR_FORMAT_S)
                + String.format("%07d", seqId % 10000000);
    }
}
