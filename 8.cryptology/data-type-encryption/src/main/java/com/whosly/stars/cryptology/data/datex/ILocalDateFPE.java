package com.whosly.stars.cryptology.data.datex;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

/**
 * LocalDate FPE 算法实现
 *
 * 1. java.util.Date 表示一个特定的瞬间，精确到毫秒。时区处理混乱，其 toString() 方法会隐式地使用JVM的默认时区进行显示。
 *           Date  在 Java 中通常表示为自 1970-01-01 00:00:00 GMT 以来的毫秒数（一个  Long）。
 * 2. java.time.LocalDate 表示一个不带时区的纯日期，例如生日、节日。只有 年-月-日. 时区处理混乱，明确表示无时区信息，就是本地日历系统中的日期。
 *
 * @author fengyang
 * @date 2025-10-27 17:06:01
 */
public interface ILocalDateFPE {

    /**
     * 加密 Date
     */
    Date encrypt(Date date);

    /**
     * 解密 Date
     */
    Date decrypt(Date encryptedDate);

    /**
     * 批量加密
     */
    Date[] encryptBatch(Date[] dates);

    /**
     * 批量解密
     */
    Date[] decryptBatch(Date[] encryptedDates);

    /**
     * 加密 LocalDate
     */
    LocalDate encrypt(LocalDate localDate);

    /**
     * 解密 LocalDate
     */
    LocalDate decrypt(LocalDate encryptedDate);

    /**
     * 批量加密
     */
    LocalDate[] encryptBatch(LocalDate[] dates);

    /**
     * 批量解密
     */
    LocalDate[] decryptBatch(LocalDate[] encryptedDates);

    LocalDate getMinDate();
    LocalDate getMaxDate();
    BigInteger getTotalDays();

    /**
     * 安全擦除密钥
     */
    void secureErase();

}
