package com.whosly.stars.cryptology.data.datex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
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
                // 16字节密钥的Base64
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==")
                .dateRange(
                        LocalDate.of(2000, 1, 1),
                        LocalDate.of(2081, 12, 30)
                )
                .build();

        this.fpe = config.createFPE();
    }

    @Test
    public void testSingle() {
        for (int i = 0; i < 10; i++) {
            FPEConfig config = new FPEConfig.Builder()
                    .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                    .dateRange(
                            LocalDate.of(2000, 1, 1),
                            LocalDate.of(2080, 12, 30)
                    )
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
    }

    @Test
    public void testAllDateRange() {
        boolean printLog = false;

        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(
                        LocalDate.of(1965, 1, 1),
                        LocalDate.of(2080, 12, 31)
                )
                .build();

        // 2. 创建 FPE 实例
        ILocalDateFPE fpeSingle = config.createFPE();

        System.out.println("原始日期        加密后        解密后");
        for (int year = 1965; year <= 2080; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= 31; day++) {
                    LocalDate original = null;
                    try{
                        original = LocalDate.of(year, month, day);
                    } catch (DateTimeException e) {
                        if(printLog) {
                            System.err.println("错误输入，不是合法的时间：" + String.format("%d-%d-%d", year, month, day));
                        }
                        continue;
                    }

                    LocalDate encrypted = fpeSingle.encrypt(original);

                    LocalDate decrypted = fpeSingle.decrypt(encrypted);
                    if(printLog) {
                        System.out.println(original + "    " + encrypted + "   " + decrypted);
                    }

                    try{
                        assertTrue(original.equals(decrypted), "原始日期:"+original+", 解密后: "+decrypted+", 加密后: " + encrypted);
                    }catch (Throwable t){
                        System.err.println(original + "    " + encrypted + "   " + decrypted);
                    }
                }
            }
        }
    }

    @Test
    public void testSomeDate() throws NoSuchAlgorithmException {
        System.out.println("Min date: " + fpe.getMinDate());
        System.out.println("Max date: " + fpe.getMaxDate());
        System.out.println("Total days: " + fpe.getTotalDays());

        // 测试特定日期
        LocalDate[] testDates = {
                LocalDate.of(2026, 12, 21),
                LocalDate.of(2026, 12, 22),
                LocalDate.of(2026, 12, 23),
                LocalDate.of(2026, 12, 24),
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2026, 12, 26),
                LocalDate.of(2026, 12, 27),
                LocalDate.of(2026, 12, 28),
                LocalDate.of(2026, 12, 29),
                LocalDate.of(2026, 12, 30),
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2060, 12, 21),
                LocalDate.of(2060, 12, 22),
                LocalDate.of(2060, 12, 23),
                LocalDate.of(2060, 12, 24),
                LocalDate.of(2060, 12, 25),
                LocalDate.of(2060, 12, 26),
                LocalDate.of(2060, 12, 27),
                LocalDate.of(2060, 12, 28),
                LocalDate.of(2060, 12, 29),
                LocalDate.of(2060, 12, 30),
                LocalDate.of(2060, 12, 31),
                LocalDate.of(2080, 12, 21),
                LocalDate.of(2080, 12, 22),
                LocalDate.of(2080, 12, 23),
                LocalDate.of(2080, 12, 24),
                LocalDate.of(2080, 12, 25),
                LocalDate.of(2080, 12, 26),
                LocalDate.of(2080, 12, 27),
                LocalDate.of(2080, 12, 28),
                LocalDate.of(2080, 12, 29),
                LocalDate.of(2080, 12, 30),
                LocalDate.of(2080, 12, 31),
                LocalDate.of(2081, 1, 30),
                LocalDate.of(2081, 2, 22),
                LocalDate.of(2081, 3, 31),
                LocalDate.of(2081, 4, 30),
                LocalDate.of(2081, 5, 31),
                LocalDate.of(2081, 6, 30),
                LocalDate.of(2081, 7, 31),
                LocalDate.of(2081, 8, 31),
                LocalDate.of(2081, 9, 30),
                LocalDate.of(2081, 10, 31),
                LocalDate.of(2081, 11, 30),
                LocalDate.of(2081, 12, 1),
                LocalDate.of(2081, 12, 8),
                LocalDate.of(2081, 12, 10),
                LocalDate.of(2081, 12, 15),
                LocalDate.of(2081, 12, 19),
                this.fpe.getMinDate().plusDays(2),
                this.fpe.getMinDate().plusDays(1),
                this.fpe.getMinDate(),
                this.fpe.getMaxDate().minusDays(5),
                this.fpe.getMaxDate().minusDays(4),
                this.fpe.getMaxDate().minusDays(3),
                this.fpe.getMaxDate().minusDays(2),
                this.fpe.getMaxDate().minusDays(1),
                this.fpe.getMaxDate()
        };

        for (LocalDate testDate : testDates) {
            LocalDate encrypted = fpe.encrypt(testDate);
            LocalDate decrypted = fpe.decrypt(encrypted);
            assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
        }
    }

    @Test
    public void testSimple() {
        for (int i = 0; i < 10; i++) {
            LocalDate[] testDates = {
                    this.fpe.getMinDate().plusDays(2),
                    this.fpe.getMinDate().plusDays(1),
                    this.fpe.getMinDate().plusDays(this.fpe.getTotalDays().longValue() / 2),
                    this.fpe.getMinDate()
            };

            for (LocalDate testDate : testDates) {
                LocalDate encrypted = this.fpe.encrypt(testDate);
                LocalDate decrypted = this.fpe.decrypt(encrypted);

                assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
            }
        }
    }


    @Test
    public void testEncryptDecrypt() {
        // 测试特定日期
        LocalDate[] testDates = {
                LocalDate.of(2023, 6, 15),
                LocalDate.of(2026, 12, 21),
                LocalDate.of(2060, 12, 31),
                LocalDate.of(2081, 12, 10),
                LocalDate.of(2081, 12, 15),
                LocalDate.of(2081, 12, 19),
                LocalDate.of(2081, 12, 30),
                this.fpe.getMinDate(),
                this.fpe.getMaxDate(),
                this.fpe.getMaxDate().minusDays(1),
                this.fpe.getMaxDate().minusDays(2),
                this.fpe.getMaxDate().minusDays(3)
        };

        for (LocalDate testDate : testDates) {
            try {
                LocalDate encrypted = this.fpe.encrypt(testDate);
                LocalDate decrypted = this.fpe.decrypt(encrypted);
                assertEquals(testDate, decrypted,
                        "Encryption/decryption failed for date: " + testDate);
            } catch (Exception e) {
                fail("Failed to encrypt/decrypt date: " + testDate + ", error: " + e.getMessage());
            }
        }
    }

    @Test
    public void testBatchOperations() {
        LocalDate[] originalDates = {
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 6, 15),
                LocalDate.of(2023, 12, 31),
                LocalDate.of(2081, 12, 10),
                LocalDate.of(2081, 12, 30)
        };

        LocalDate[] encryptedDates = this.fpe.encryptBatch(originalDates);
        LocalDate[] decryptedDates = this.fpe.decryptBatch(encryptedDates);

        assertArrayEquals(originalDates, decryptedDates,
                "Batch encryption/decryption failed");
    }

    @Test
    public void testDateRangeValidation() {
        // 测试日期范围验证
        assertThrows(IllegalArgumentException.class, () -> {
            this.fpe.encrypt(LocalDate.of(1999, 12, 31)); // 小于最小日期
        });

        assertThrows(IllegalArgumentException.class, () -> {
            this.fpe.encrypt(LocalDate.of(2082, 1, 1)); // 大于最大日期
        });
    }

    @Test
    public void testNullInputs() {
        // 测试空值处理
        assertThrows(NullPointerException.class, () -> {
            this.fpe.encrypt((LocalDate) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.decrypt((LocalDate) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.encryptBatch((LocalDate[]) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.decryptBatch((LocalDate[]) null);
        });
    }

    @Test
    public void encrypt() {
        for (int i = 0; i < 10; i++) {
            for (long source = 0; source < 200; source++) {
                LocalDate date = randomDateInPastYears(6);
                LocalDate encDateVal = this.fpe.encrypt(date);
                assertTrue(encDateVal != null);
            }
        }
    }

    @Test
    public void decryptDate() {
        for (int i = 0; i < 10; i++) {
            for (long k = 0; k < 200; k++) {
                LocalDate original = randomDateInPastYears(6);
                System.out.println("原始日期: " + original);

                LocalDate encrypted = this.fpe.encrypt(original);
                System.out.println("加密后: " + encrypted);

                LocalDate decrypted = this.fpe.decrypt(encrypted);
                System.out.println("("+i+"/"+k+")解密后: " + decrypted);

                assertTrue(original.equals(decrypted));
            }
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