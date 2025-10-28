package com.whosly.stars.cryptology.data.datex.impl;

import com.whosly.stars.cryptology.data.datex.ILocalDatexFPE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

/**
 * @author fengyang
 * @date 2025-10-28 15:05:02
 * @description
 */
class DefaultLocalDatexFPEImpl extends DefaultDateFPEImpl implements ILocalDatexFPE {

    public DefaultLocalDatexFPEImpl(byte[] key, Date minDate, Date maxDate) {
        super(key, minDate, maxDate);
    }

    public DefaultLocalDatexFPEImpl(byte[] key, LocalDate minDate, LocalDate maxDate) {
        super(key, minDate, maxDate);
    }

    /**
     * 加密 LocalDateTime
     */
    @Override
    public LocalDateTime encrypt(LocalDateTime localDateTime) {
        // 将LocalDateTime转换为LocalDate进行日期部分加密
        LocalDate localDate = localDateTime.toLocalDate();
        LocalDate encryptedDate = encrypt(localDate); // 复用LocalDate的加密逻辑

        // 保持时间部分不变
        return LocalDateTime.of(encryptedDate, localDateTime.toLocalTime());
    }

    /**
     * 解密为 LocalDateTime
     */
    @Override
    public LocalDateTime decrypt(LocalDateTime encryptedDateTime) {
        // 将LocalDateTime转换为LocalDate进行日期部分解密
        LocalDate localDate = encryptedDateTime.toLocalDate();
        LocalDate decryptedDate = decrypt(localDate); // 复用LocalDate的解密逻辑

        // 保持时间部分不变
        return LocalDateTime.of(decryptedDate, encryptedDateTime.toLocalTime());
    }



    /**
     * 加密日期
     *
     * @param localDate 要加密的日期
     * @return 加密后的日期
     */
    @Override
    public LocalDate encrypt(LocalDate localDate) {
        Objects.requireNonNull(localDate, "localDate cannot be null");
//        validateDateInRange(localDate);

        // 将日期转换为相对于最小日期的天数偏移量
        long dayOffset = ChronoUnit.DAYS.between(getMinDate(), localDate);

        // 使用LongFPE加密偏移量
        long encryptedOffset = this.fpeEngine.encrypt(dayOffset);

        return getMinDate().plusDays(encryptedOffset);
    }

    /**
     * 解密日期
     *
     * @param encryptedDate 加密的日期
     * @return 解密后的日期
     */
    @Override
    public LocalDate decrypt(LocalDate encryptedDate) {
        Objects.requireNonNull(encryptedDate, "Encrypted date cannot be null");
//        validateDateInRange(encryptedDate);

        // 将加密日期转换为相对于最小日期的天数偏移量
        long encryptedOffset = ChronoUnit.DAYS.between(getMinDate(), encryptedDate);

        // 使用LongFPE解密偏移量
        long decryptedOffset = this.fpeEngine.decrypt(encryptedOffset);

        return getMinDate().plusDays(decryptedOffset);
    }

    /**
     * 批量加密日期
     *
     * @param dates 要加密的日期数组
     * @return 加密后的日期数组
     */
    @Override
    public LocalDate[] encryptBatch(LocalDate[] dates) {
        Objects.requireNonNull(dates, "Dates array cannot be null");

        LocalDate[] result = new LocalDate[dates.length];
        for (int i = 0; i < dates.length; i++) {
            result[i] = encrypt(dates[i]);
        }
        return result;
    }

    /**
     * 批量解密日期
     *
     * @param encryptedDates 加密的日期数组
     * @return 解密后的日期数组
     */
    @Override
    public LocalDate[] decryptBatch(LocalDate[] encryptedDates) {
        Objects.requireNonNull(encryptedDates, "Encrypted dates array cannot be null");

        LocalDate[] result = new LocalDate[encryptedDates.length];
        for (int i = 0; i < encryptedDates.length; i++) {
            result[i] = decrypt(encryptedDates[i]);
        }
        return result;
    }
}
