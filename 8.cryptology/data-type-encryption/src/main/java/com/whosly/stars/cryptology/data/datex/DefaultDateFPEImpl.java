package com.whosly.stars.cryptology.data.datex;

import cn.hutool.core.date.DateUtil;
import com.whosly.stars.cryptology.data.longx.LongFPECrypto;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

/**
 * 基于Hutool FPE实现的日期格式保留加密
 *
 * @author fengyang
 * @date 2025-10-28
 */
public class DefaultDateFPEImpl implements ILocalDateFPE{
//    private final FPE numericFpe;
    private final LongFPECrypto longFPECrypto;

    private final LocalDate minDate;
    private final LocalDate maxDate;
    private final BigInteger totalDays;
    
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

        this.minDate = minDate;
        this.maxDate = maxDate;
        this.totalDays = calculateTotalDays(minDate, maxDate);
//        this.totalDays = ChronoUnit.DAYS.between(minDate, maxDate) + 1;

        try {
            this.longFPECrypto = new LongFPECrypto();
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
//        validateDateInRange(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        // 将日期转换为相对于最小日期的天数偏移量
        long dayTime = date.getTime();

        // 使用LongFPE加密偏移量
        long encryptedDayTime = longFPECrypto.encrypt(dayTime);

        return new Date(encryptedDayTime);
    }

    @Override
    public Date decrypt(Date encryptedDate) {
        Objects.requireNonNull(encryptedDate, "Encrypted date cannot be null");
//        validateDateInRange(encryptedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        long encryptedDayTime = encryptedDate.getTime();

        // 使用LongFPE加密偏移量
        long decryptedDayTime = longFPECrypto.decrypt(encryptedDayTime);
        return new Date(decryptedDayTime);
    }

    @Override
    public Date[] encryptBatch(Date[] dates) {
        Objects.requireNonNull(dates, "Dates array cannot be null");

        Date[] result = new Date[dates.length];
        for (int i = 0; i < dates.length; i++) {
            result[i] = encrypt(dates[i]);
        }
        return result;
    }

    @Override
    public Date[] decryptBatch(Date[] encryptedDates) {
        Objects.requireNonNull(encryptedDates, "Encrypted dates array cannot be null");

        Date[] result = new Date[encryptedDates.length];
        for (int i = 0; i < encryptedDates.length; i++) {
            result[i] = decrypt(encryptedDates[i]);
        }
        return result;
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

        Date encryptDate = encrypt(DateUtil.beginOfDay(DateUtil.date(localDate)));

        return DateUtil.toLocalDateTime(encryptDate).toLocalDate();
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

        Date decryptDate = decrypt(DateUtil.beginOfDay(DateUtil.date(encryptedDate)));

        return DateUtil.toLocalDateTime(decryptDate).toLocalDate();
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

    @Override
    public LocalDate getMinDate() {
        return minDate;
    }

    @Override
    public LocalDate getMaxDate() {
        return maxDate;
    }

    @Override
    public BigInteger getTotalDays() {
        return totalDays;
    }

    @Override
    public void secureErase() {

    }

    @Override
    protected void finalize() throws Throwable {
        secureErase();

        super.finalize();
    }

    /**
     * 验证日期是否在范围内
     *
     * @param date 要验证的日期
     */
    private void validateDateInRange(LocalDate date) {
        if (date.isBefore(minDate)) {
            throw new IllegalArgumentException(
                    String.format("Date %s is before minimum date %s", date, minDate));
        }
        if (date.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                    String.format("Date %s is after maximum date %s", date, maxDate));
        }
    }

    private BigInteger calculateTotalDays(LocalDate min, LocalDate max) {
        long days = ChronoUnit.DAYS.between(min, max) + 1;
        if (days <= 0) {
            throw new IllegalArgumentException("Date range must contain at least one day");
        }
        return BigInteger.valueOf(days);
    }
}