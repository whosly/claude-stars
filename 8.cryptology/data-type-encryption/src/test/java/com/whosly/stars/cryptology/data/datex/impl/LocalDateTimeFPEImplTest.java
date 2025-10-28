package com.whosly.stars.cryptology.data.datex.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import com.whosly.stars.cryptology.data.datex.ILocalDatexFPE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author fengyang
 * @date 2025-10-28 14:34:09
 * @description
 */
public class LocalDateTimeFPEImplTest {

    private ILocalDatexFPE fpe;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        // 1. 配置 FPE
        FPEConfig config = new FPEConfig.Builder()
                // 16字节密钥的Base64
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==")
                .dateRange(
                        LocalDate.of(1965, 1, 1),
                        LocalDate.of(2080, 12, 31)
                )
                .build();

        this.fpe = config.createLocalDateFPE();
    }

    @Test
    public void testSingle() {
        for (int i = 0; i < 10; i++) {
            // 4. 使用加密解密
            LocalDateTime original = LocalDateTime.of(2080, 12, 30, 23, 59); // 确保在范围内
            System.out.println("原始日期: " + original);

            LocalDateTime encrypted = fpe.encrypt(original);
            System.out.println("加密后: " + encrypted);

            LocalDateTime decrypted = fpe.decrypt(encrypted);
            System.out.println("解密后: " + decrypted);
            System.out.println("匹配: " + original.equals(decrypted));
            assertTrue(original.equals(decrypted), "原始日期:"+original+", 解密后: "+decrypted+", 加密后: " + encrypted);
        }
    }

    @Test
    public void testAllDateRange() {
        boolean printLog = false;

        System.out.println("原始日期        加密后        解密后");
        for (int year = 1965; year <= 2025; year++) { // 限制年份范围以减少测试时间
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= 28; day++) { // 限制为28天以避免无效日期
                    LocalDateTime original = null;
                    try{
                        original = LocalDateTime.of(year, month, day, RandomUtil.randomInt(0, 23), RandomUtil.randomInt(0, 59));
                    } catch (DateTimeException e) {
                        if(printLog) {
                            System.err.println("错误输入，不是合法的时间：" + String.format("%d-%d-%d", year, month, day));
                        }
                        continue;
                    }

                    LocalDateTime encrypted = fpe.encrypt(original);

                    LocalDateTime decrypted = fpe.decrypt(encrypted);
                    if(printLog) {
                        System.out.println(original + "    " + encrypted + "   " + decrypted);
                    }

                    assertTrue(original.equals(decrypted), "原始日期:"+original+", 解密后: "+decrypted+", 加密后: " + encrypted);
                }
            }
        }
    }

    @Test
    public void testSomeDate() throws NoSuchAlgorithmException {
        System.out.println("Min date: " + fpe.getMinDate());
        System.out.println("Max date: " + fpe.getMaxDate());

        // 测试特定日期
        LocalDateTime[] testDates = {
                LocalDateTime.of(2026, 12, 21, RandomUtil.randomInt(0, 23), RandomUtil.randomInt(0, 59)),
                LocalDateTime.of(2026, 12, 22, RandomUtil.randomInt(0, 23), RandomUtil.randomInt(0, 59)),
                LocalDateTime.of(2026, 12, 21, 0, 0),
                LocalDateTime.of(2026, 12, 22, 0, 0),
                LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30).plusDays(2),
                LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30).plusDays(1),
                LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(5),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(4),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(3),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(2),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(1),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30)
        };

        for (LocalDateTime testDate : testDates) {
            LocalDateTime encrypted = fpe.encrypt(testDate);
            LocalDateTime decrypted = fpe.decrypt(encrypted);
            assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
        }
    }

    @Test
    public void testSimple() {
        for (int i = 0; i < 10; i++) {
            LocalDateTime[] testDates = {
                    LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30).plusDays(2),
                    LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30).plusDays(1),
                    LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30)
            };

            for (LocalDateTime testDate : testDates) {
                LocalDateTime encrypted = this.fpe.encrypt(testDate);
                LocalDateTime decrypted = this.fpe.decrypt(encrypted);

                assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
            }
        }
    }

    @Test
    public void testEncryptDecrypt() {
        // 测试特定日期
        LocalDateTime[] testDates = {
                LocalDateTime.of(2023, 6, 15, 12, 30),
                LocalDateTime.of(2026, 12, 21, 12, 30),
                LocalDateTime.of(2060, 12, 31, 12, 30),
                LocalDateTime.of(2080, 12, 10, 12, 30), // 确保在范围内
                LocalDateTime.of(2080, 12, 15, 12, 30),
                LocalDateTime.of(2080, 12, 19, 12, 30),
                LocalDateTime.of(2080, 12, 30, 12, 30),
                LocalDateTime.of(this.fpe.getMinDate().getYear(), this.fpe.getMinDate().getMonthValue(), this.fpe.getMinDate().getDayOfMonth(), 12, 30),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(1),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(2),
                LocalDateTime.of(this.fpe.getMaxDate().getYear(), this.fpe.getMaxDate().getMonthValue(), this.fpe.getMaxDate().getDayOfMonth(), 12, 30).minusDays(3)
        };

        for (LocalDateTime testDate : testDates) {
            try {
                LocalDateTime encrypted = this.fpe.encrypt(testDate);
                LocalDateTime decrypted = this.fpe.decrypt(encrypted);
                assertEquals(testDate, decrypted,
                        "Encryption/decryption failed for date: " + testDate);
            } catch (Exception e) {
                fail("Failed to encrypt/decrypt date: " + testDate + ", error: " + e.getMessage());
            }
        }
    }

    @Test
    public void testNullInputs() {
        // 测试空值处理
        assertThrows(NullPointerException.class, () -> {
            this.fpe.encrypt((LocalDateTime) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.decrypt((LocalDateTime) null);
        });
    }

    @Test
    public void encrypt() {
        List<LocalDateTime> randomDate = randomLocalDateTime();

        randomDate.parallelStream().forEach(d -> {
            LocalDateTime encDateVal = this.fpe.encrypt(d);
            assertTrue(encDateVal != null);
        });
    }

    @Test
    public void decryptDate() {
        for (int i = 0; i < 2; i++) {
            List<LocalDateTime> randomDate = randomLocalDateTime();

            randomDate.parallelStream().forEach(d -> {
                LocalDateTime encrypted = this.fpe.encrypt(d);
                LocalDateTime decrypted = this.fpe.decrypt(encrypted);

                assertTrue(d.equals(decrypted), "original:"+d+", decrypted: "+decrypted+", encrypted: " + encrypted);
            });
        }
    }

    private static List<LocalDateTime> randomLocalDateTime() {
        List<LocalDateTime> result = new ArrayList<>();
        for (int i = 0; i < 200; i++) { // 减少测试数据量
            DateTime randomDateTime = RandomUtil.randomDay(-365 * (2025-1965), 365 * (2080-2025));
            LocalDateTime localDateTime = randomDateTime.toTimestamp().toLocalDateTime();
            result.add(localDateTime);
        }
        return result;
    }

}
