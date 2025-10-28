package com.whosly.stars.cryptology.data.datex;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * java.time.LocalDate FPE 算法实现:
 *
 * 1. java.time.LocalDate 表示一个不带时区的纯日期，例如生日、节日。只有 年-月-日. 时区处理混乱，明确表示无时区信息，就是本地日历系统中的日期。
 * 2. java.time.LocalDateTime 表示年月日和时分秒，没有时区。
 * 
 * @author fengyang
 * @date 2025-10-28 15:07:06
 */
public interface ILocalDatexFPE extends IDateFPE {
    /**
     * 加密 LocalDateTime
     */
    LocalDateTime encrypt(LocalDateTime localDateTime);

    /**
     * 解密 LocalDateTime
     */
    LocalDateTime decrypt(LocalDateTime encryptedDate);

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
}
