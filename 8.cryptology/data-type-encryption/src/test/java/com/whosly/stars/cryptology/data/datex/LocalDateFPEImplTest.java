package com.whosly.stars.cryptology.data.datex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-10-27 17:04:41
 * @description
 */
class LocalDateFPEImplTest {

    private ILocalDateFPE fpe;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        // 1. 配置 FPE
        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(
                        LocalDate.of(2000, 1, 1),
                        LocalDate.of(2081, 12, 30)
                )
                .rounds(10)
                .build();

        this.fpe = config.createFPE();
    }

    @Test
    public void testSingle() {
        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(
                        LocalDate.of(2000, 1, 1),
                        LocalDate.of(2080, 12, 30)
                )
                .rounds(10)
                .build();

        // 2. 创建 FPE 实例
        ILocalDateFPE fpeSingle = config.createFPE();

        // 4. 使用加密解密
        LocalDate original = LocalDate.of(2023, 6, 15);
        System.out.println("原始日期: " + original);

        LocalDate encrypted = fpeSingle.encrypt(original);
        System.out.println("加密后: " + encrypted);

        LocalDate decrypted = fpeSingle.decrypt(encrypted);
        System.out.println("解密后: " + decrypted);
        System.out.println("匹配: " + original.equals(decrypted));

        // 5. 批量操作
        LocalDate[] dates = {
                LocalDate.of(2005, 3, 10),
                LocalDate.of(2010, 8, 25),
                LocalDate.of(2015, 12, 5)
        };

        LocalDate[] encryptedDates = fpeSingle.encryptBatch(dates);
        LocalDate[] decryptedDates = fpeSingle.decryptBatch(encryptedDates);
        System.out.println("批量操作验证: " + java.util.Arrays.equals(dates, decryptedDates));
    }

    @Test
    public void testSimple() {
        LocalDate[] testDates = {
                this.fpe.getMinDate().plusDays(2),
                this.fpe.getMinDate().plusDays(1),
                this.fpe.getMaxDate().minusDays(2),
                this.fpe.getMaxDate().minusDays(1),
                this.fpe.getMinDate().plusDays(this.fpe.getTotalDays().longValue() / 2),
                this.fpe.getMaxDate(),
                this.fpe.getMinDate()
        };

        for (LocalDate testDate : testDates) {
            LocalDate encrypted = this.fpe.encrypt(testDate);
            LocalDate decrypted = this.fpe.decrypt(encrypted);

            assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
        }
    }

    @Test
    public void encrypt() {
        for (long source = 0; source < 200; source++) {
            LocalDate date = randomDateInPastYears(6);
            LocalDate encDateVal = this.fpe.encrypt(date);
            assertTrue(encDateVal != null);
        }
    }

    @Test
    public void decryptDate() {
        for (long i = 0; i < 200; i++) {
            LocalDate original = randomDateInPastYears(6);
            System.out.println("原始日期: " + original);

            LocalDate encrypted = this.fpe.encrypt(original);
            System.out.println("加密后: " + encrypted);

            LocalDate decrypted = this.fpe.decrypt(encrypted);
            System.out.println("(" + i + ")解密后: " + decrypted);

            assertTrue(original.equals(decrypted));
        }
    }

    /**
     * 生成过去N年内的随机日期
     */
    private static LocalDate randomDateInPastYears(int years) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(years);
        return randomDate(startDate, endDate);
    }

    /**
     * 生成未来N年内的随机日期
     */
    private static LocalDate randomDateInFutureYears(int years) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusYears(years);
        return randomDate(startDate, endDate);
    }

    /**
     * 生成指定年份内的随机日期
     */
    private static LocalDate randomDateInYear(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return randomDate(startDate, endDate);
    }

    /**
     * 在指定范围内随机生成日期
     */
    private static LocalDate randomDate(LocalDate startDate, LocalDate endDate) {
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDay);
    }
}