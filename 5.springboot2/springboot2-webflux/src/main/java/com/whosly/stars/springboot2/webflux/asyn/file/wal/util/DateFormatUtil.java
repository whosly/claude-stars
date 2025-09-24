package com.whosly.stars.springboot2.webflux.asyn.file.wal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateFormatUtil {

    public static final String format() {
        return format(DateFormatType.FORMAT_MS);
    }

    public static final String format(DateFormatType formatType) {
        return format(formatType.getFormat());
    }

    private static final String format(SimpleDateFormat format) {
        return format.format(new Date());
    }
}
