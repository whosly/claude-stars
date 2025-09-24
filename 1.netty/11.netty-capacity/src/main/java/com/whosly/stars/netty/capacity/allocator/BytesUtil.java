package com.whosly.stars.netty.capacity.allocator;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author fengyang
 * @date 2023/8/10 下午3:24
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
class BytesUtil
{
    /**
     * 数值类型 转 KB,MB,GB...
     */
    public static String byteToM(Integer size) {
        return byteToM(size.longValue());
    }

    /**
     * 数值类型 转 KB,MB,GB...
     */
    public static String byteToM(Long size) {
        BigDecimal fileSize = new BigDecimal(size);
        BigDecimal param = new BigDecimal(1024);
        int dep = 0;
        while(fileSize.compareTo(param) > 0 && dep < 5)
        {
            fileSize = fileSize.divide(param);
            dep++;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        String result = df.format(fileSize) + "";
        switch (dep) {
            case 0:
                result += "B";
                break;
            case 1:
                result += "KB";
                break;
            case 2:
                result += "MB";
                break;
            case 3:
                result += "GB";
                break;
            case 4:
                result += "TB";
                break;
            case 5:
                result += "PB";
                break;
        }
        return result;
    }
}
