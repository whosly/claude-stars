package com.whosly.stars.cryptology.data.datex;

import com.whosly.stars.cryptology.data.common.fpe.IFPE;

import java.time.LocalDate;
import java.util.Date;

/**
 * java.util.Date FPE 算法实现: java.util.Date 表示一个特定的瞬间，精确到毫秒。时区处理混乱，其 toString() 方法会隐式地使用JVM的默认时区进行显示。
 *
 * Date  在 Java 中通常表示为自 1970-01-01 00:00:00 GMT 以来的毫秒数（一个  Long）。
 *
 * @author fengyang
 * @date 2025-10-27 17:06:01
 */
public interface IDateFPE extends IFPE<Date>  {
//
//    /**
//     * 加密 Date
//     */
//    Date encrypt(Date date);
//
//    /**
//     * 解密 Date
//     */
//    Date decrypt(Date encryptedDate);
//
//    /**
//     * 批量加密
//     */
//    Date[] encryptBatch(Date[] dates);
//
//    /**
//     * 批量解密
//     */
//    Date[] decryptBatch(Date[] encryptedDates);

    LocalDate getMinDate();
    LocalDate getMaxDate();

    /**
     * 安全擦除密钥
     */
    void secureErase();

}
