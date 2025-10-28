package com.whosly.stars.cryptology.data.datex.impl;

import com.whosly.stars.cryptology.data.datex.IDateFPE;
import com.whosly.stars.cryptology.data.longx.LongFPECrypto;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * 基于Hutool FPE实现的日期格式保留加密
 *
 * @author fengyang
 * @date 2025-10-28
 */
class DefaultDateFPEImpl implements IDateFPE {
    protected final LongFPECrypto fpeEngine;

    private final byte[] key;
    private final LocalDate minDate;
    private final LocalDate maxDate;
    private final long totalDays;
    
    /**
     * 构造函数
     * 
     * @param key AES密钥，长度必须是16bytes、24bytes或32bytes
     * @param minDate 最小日期
     * @param maxDate 最大日期
     */
    public DefaultDateFPEImpl(byte[] key, Date minDate, Date maxDate) {
        this(key, minDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * 构造函数
     *
     * @param key AES密钥，长度必须是16bytes、24bytes或32bytes
     * @param minDate 最小日期
     * @param maxDate 最大日期
     */
    public DefaultDateFPEImpl(byte[] key, LocalDate minDate, LocalDate maxDate) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(minDate, "Min date cannot be null");
        Objects.requireNonNull(maxDate, "Max date cannot be null");

        if (minDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("Min date must be before max date");
        }

        this.key = key.clone();
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.totalDays = calculateTotalDays(minDate, maxDate);
//        this.totalDays = calculateTotalDays(minDate, maxDate);
//        this.totalDays = ChronoUnit.DAYS.between(minDate, maxDate) + 1;

        try {
            this.fpeEngine = new LongFPECrypto();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize LongFPECrypto", e);
        }
//
//        Map<CharacterSet, FPE> fpeMap =  FPEAlphabetFactory.getFF1(key.clone(), null);
//        this.numericFpe = fpeMap.get(CharacterSet.NUMERIC);
    }

    @Override
    public Date encrypt(Date date) {
        Objects.requireNonNull(date, "Date cannot be null");

        return encryptWithMills(date);
    }

    @Override
    public Date decrypt(Date encryptedDate) {
        Objects.requireNonNull(encryptedDate, "Encrypted date cannot be null");

        return decryptWithMills(encryptedDate);
    }

    @Override
    public Date[] encryptBatch(Date[] dates) {
        Objects.requireNonNull(dates, "Dates array cannot be null");

        return Arrays.stream(dates)
                .parallel()
                .map(this::encrypt)
                .toArray(Date[]::new);
    }

    @Override
    public Date[] decryptBatch(Date[] encryptedDates) {
        Objects.requireNonNull(encryptedDates, "Encrypted dates array cannot be null");

        return Arrays.stream(encryptedDates)
                .parallel()
                .map(this::decrypt)
                .toArray(Date[]::new);
    }

    @Override
    public LocalDate getMinDate() {
        return minDate;
    }

    @Override
    public LocalDate getMaxDate() {
        return maxDate;
    }

    @Override
    public void secureErase() {
        if (key != null) {
            Arrays.fill(key, (byte) 0);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        secureErase();

        super.finalize();
    }

    /**
     * 加密时间戳
     */
    private Date encryptWithMills(Date date) {
        long dayTime = date.getTime();

        // 不进行严格的范围验证，因为FPE加密可能产生任意值
        long encryptedDayTime = fpeEngine.encrypt(dayTime);
        return new Date(encryptedDayTime);
    }

    /**
     * 解密时间戳
     */
    private Date decryptWithMills(Date encryptedDate) {
        long encryptedDayTime = encryptedDate.getTime();

        long decryptedDayTime = fpeEngine.decrypt(encryptedDayTime);
        return new Date(decryptedDayTime);
    }

    /**
     * 安全计算总天数，防止溢出
     */
    private long calculateTotalDays(LocalDate min, LocalDate max) {
        try {
            long days = ChronoUnit.DAYS.between(min, max) + 1;
            if (days <= 0) {
                throw new IllegalArgumentException("Date range must contain at least one day");
            }
            // 检查是否在合理范围内
            if (days > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Date range too large: " + days + " days");
            }
            return days;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Date range calculation overflow", e);
        }
    }
}