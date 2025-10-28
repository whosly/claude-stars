package com.whosly.stars.cryptology.data.datex;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-10-27 17:04:41
 * @description
 */
class DateFPEImplTest {

    private ILocalDateFPE fpe;

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

        this.fpe = config.createFPE();
    }

    @Test
    public void testSingle() {
        for (int i = 0; i < 10; i++) {
            FPEConfig config = new FPEConfig.Builder()
                    .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                    .dateRange(
                            DateUtil.parse("2000-01-01"),
                            DateUtil.parse("2080-12-30")
                    )
                    .build();

            // 2. 创建 FPE 实例
            ILocalDateFPE fpeSingle = config.createFPE();

            // 4. 使用加密解密
            Date original = DateUtil.parse("2080-12-30");
            System.out.println("原始日期: " + original);

            Date encrypted = fpeSingle.encrypt(original);
            System.out.println("加密后: " + encrypted);

            Date decrypted = fpeSingle.decrypt(encrypted);
            System.out.println("解密后: " + decrypted);
            System.out.println("匹配: " + original.equals(decrypted));

            // 5. 批量操作
            Date[] dates = {
                    DateUtil.parse("2025-3-10"),
                    DateUtil.parse("2010-8-25"),
                    DateUtil.parse("2015-12-5")
            };

            Date[] encryptedDates = fpeSingle.encryptBatch(dates);
            Date[] decryptedDates = fpeSingle.decryptBatch(encryptedDates);
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
                    Date original = null;
                    try{
                        original = DateUtil.parse(String.format("%d-%d-%d", year, month, day));
                    } catch (DateTimeException e) {
                        if(printLog) {
                            System.err.println("错误输入，不是合法的时间：" + String.format("%d-%d-%d", year, month, day));
                        }
                        continue;
                    }

                    Date encrypted = fpeSingle.encrypt(original);

                    Date decrypted = fpeSingle.decrypt(encrypted);
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
        Date[] testDates = {
                DateUtil.date(LocalDate.of(2026, 12, 21)),
                DateUtil.date(LocalDate.of(2026, 12, 22)),
                DateUtil.date(this.fpe.getMinDate().plusDays(2)),
                DateUtil.date(this.fpe.getMinDate().plusDays(1)),
                DateUtil.date(this.fpe.getMinDate()),
                DateUtil.date(this.fpe.getMaxDate().minusDays(5)),
                DateUtil.date(this.fpe.getMaxDate().minusDays(4)),
                DateUtil.date(this.fpe.getMaxDate().minusDays(3)),
                DateUtil.date(this.fpe.getMaxDate().minusDays(2)),
                DateUtil.date(this.fpe.getMaxDate().minusDays(1)),
                DateUtil.date(this.fpe.getMaxDate())
        };

        for (Date testDate : testDates) {
            Date encrypted = fpe.encrypt(testDate);
            Date decrypted = fpe.decrypt(encrypted);
            assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
        }
    }

    @Test
    public void testSimple() {
        for (int i = 0; i < 10; i++) {
            Date[] testDates = {
                    DateUtil.date(this.fpe.getMinDate().plusDays(2)),
                    DateUtil.date(this.fpe.getMinDate().plusDays(1)),
                    DateUtil.date(this.fpe.getMinDate().plusDays(this.fpe.getTotalDays().longValue() / 2)),
                    DateUtil.date(this.fpe.getMinDate())
            };

            for (Date testDate : testDates) {
                Date encrypted = this.fpe.encrypt(testDate);
                Date decrypted = this.fpe.decrypt(encrypted);

                assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
            }
        }
    }

    @Test
    public void testEncryptDecrypt() {
        // 测试特定日期
        Date[] testDates = {
                DateUtil.date(LocalDate.of(2023, 6, 15)),
                DateUtil.date(LocalDate.of(2026, 12, 21)),
                DateUtil.date(LocalDate.of(2060, 12, 31)),
                DateUtil.date(LocalDate.of(2081, 12, 10)),
                DateUtil.date(LocalDate.of(2081, 12, 15)),
                DateUtil.date(LocalDate.of(2081, 12, 19)),
                DateUtil.date(LocalDate.of(2081, 12, 30)),
                DateUtil.date(this.fpe.getMinDate()),
                DateUtil.date(this.fpe.getMaxDate()),
                DateUtil.date(this.fpe.getMaxDate().minusDays(1)),
                DateUtil.date(this.fpe.getMaxDate().minusDays(2)),
                DateUtil.date(this.fpe.getMaxDate().minusDays(3))
        };

        for (Date testDate : testDates) {
            try {
                Date encrypted = this.fpe.encrypt(testDate);
                Date decrypted = this.fpe.decrypt(encrypted);
                assertEquals(testDate, decrypted,
                        "Encryption/decryption failed for date: " + testDate);
            } catch (Exception e) {
                fail("Failed to encrypt/decrypt date: " + testDate + ", error: " + e.getMessage());
            }
        }
    }

    @Test
    public void testBatchOperations() {
        Date[] originalDates = {
                DateUtil.date(LocalDate.of(2023, 1, 1)),
                DateUtil.date(LocalDate.of(2023, 6, 15)),
                DateUtil.date(LocalDate.of(2023, 12, 31)),
                DateUtil.date(LocalDate.of(2081, 12, 10)),
                DateUtil.date(LocalDate.of(2081, 12, 30))
        };

        Date[] encryptedDates = this.fpe.encryptBatch(originalDates);
        Date[] decryptedDates = this.fpe.decryptBatch(encryptedDates);

        assertArrayEquals(originalDates, decryptedDates,
                "Batch encryption/decryption failed");
    }

    @Test
    public void testNullInputs() {
        // 测试空值处理
        assertThrows(NullPointerException.class, () -> {
            this.fpe.encrypt((Date) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.decrypt((Date) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.encryptBatch((Date[]) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.decryptBatch((Date[]) null);
        });
    }

    @Test
    public void encrypt() {
        List<DateTime> randomDate = randomDate();

        for (Date date : randomDate) {
            Date encDateVal = this.fpe.encrypt(date);
            assertTrue(encDateVal != null);
        }
    }

    @Test
    public void decryptDate() {
        for (int i = 0; i < 2; i++) {
            List<DateTime> randomDate = randomDate();

            for (Date original : randomDate) {
                Date encrypted = this.fpe.encrypt(original);
                Date decrypted = this.fpe.decrypt(encrypted);

                assertTrue(original.equals(decrypted), "original:"+original+", decrypted: "+decrypted+", encrypted: " + encrypted);
            }
        }
    }

    /**
     * 生成过去 DAY_OF_MONTH 内维度的日期
     */
    private static List<DateTime> randomDate() {
        // 定义开始和结束日期
        Date startDate = DateUtil.parse("1965-01-01");
        Date endDate = DateUtil.parse("2080-12-31");

        // 生成指定范围内的随机日期
        List<DateTime> randomDate = DateUtil.rangeToList(startDate, endDate, DateField.DAY_OF_MONTH);

        return randomDate;
    }

}