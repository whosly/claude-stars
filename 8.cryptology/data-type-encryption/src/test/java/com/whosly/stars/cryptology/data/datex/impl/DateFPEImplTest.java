package com.whosly.stars.cryptology.data.datex.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.whosly.stars.cryptology.data.datex.IDateFPE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-10-27 17:04:41
 * @description
 */
class DateFPEImplTest {

    private IDateFPE fpe;

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
            byte[] key = "0123456789ABCDEF".getBytes(); // 16字节密钥
            FPEConfig config = new FPEConfig.Builder()
                    .key(key)
                    .dateRange(
                            DateUtil.parse("2000-01-01"),
                            DateUtil.parse("2080-12-30")
                    )
                    .build();

            // 2. 创建 FPE 实例
            IDateFPE fpeSingle = config.createFPE();

            // 4. 使用加密解密
            Date original = DateUtil.parse("2080-12-30");
            System.out.println("原始日期: " + original);

            Date encrypted = fpeSingle.encrypt(original);
            System.out.println("加密后: " + encrypted);

            Date decrypted = fpeSingle.decrypt(encrypted);
            System.out.println("解密后: " + decrypted);
            System.out.println("匹配: " + original.equals(decrypted));
            assertTrue(original.equals(decrypted), "原始日期:"+original+", 解密后: "+decrypted+", 加密后: " + encrypted);

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
        long startTime = System.currentTimeMillis();

        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(
                        LocalDate.of(1965, 1, 1),
                        LocalDate.of(2080, 12, 31)
                )
                .build();

        // 2. 创建 FPE 实例
        IDateFPE fpeSingle = config.createFPE();

        List<LocalDate> allDates = generateAllValidDates(1000, 3800);
        System.out.println("开始进行所有日期的测试, allDates size:" + allDates.size());

//        // 并行处理
//        boolean allPassed = allDates.parallelStream()
//                .map(date -> {
//                    try {
//                        Date original = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
//                        Date encrypted = fpeSingle.encrypt(original);
//                        Date decrypted = fpeSingle.decrypt(encrypted);
//                        return original.equals(decrypted);
//                    } catch (Exception e) {
//                        System.err.println("测试失败: " + date + ", 错误: " + e.getMessage());
//                        return false;
//                    }
//                })
//                .allMatch(result -> result);
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("全量测试完成: " + allDates.size() + " 个日期, 耗时: " + (endTime - startTime) + "ms");
//
//        assertTrue(allPassed, "部分日期加解密测试失败");

//        // 使用ForkJoin框架
//        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
//
//        try {
//            boolean allPassed = forkJoinPool.submit(() ->
//                    allDates.parallelStream().allMatch(date -> {
//                        try {
//                            Date original = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
//                            Date encrypted = fpeSingle.encrypt(original);
//                            Date decrypted = fpeSingle.decrypt(encrypted);
//                            return original.equals(decrypted);
//                        } catch (Exception e) {
//                            System.err.println("测试失败: " + date);
//                            return false;
//                        }
//                    })
//            ).get();
//
//            long endTime = System.currentTimeMillis();
//            System.out.println("ForkJoin测试完成: " + allDates.size() + " 个日期, 耗时: " + (endTime - startTime) + "ms");
//
//            assertTrue(allPassed, "部分日期加解密测试失败");
//        } catch (Exception e) {
//            fail("测试执行异常: " + e.getMessage());
//        } finally {
//            forkJoinPool.shutdown();
//        }

        // 转换为Date数组
        Date[] originalDates = allDates.stream()
                .map(date -> Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .toArray(Date[]::new);

        // 批量加密
        Date[] encryptedDates = fpeSingle.encryptBatch(originalDates);

        // 批量解密
        Date[] decryptedDates = fpeSingle.decryptBatch(encryptedDates);

        // 批量验证
        boolean allPassed = true;
        for (int i = 0; i < originalDates.length; i++) {
            if (!originalDates[i].equals(decryptedDates[i])) {
                System.err.println("测试失败: " + allDates.get(i) +
                        ", 原始: " + originalDates[i] +
                        ", 解密: " + decryptedDates[i]);
                allPassed = false;
                break; // 发现错误立即停止，或者继续记录所有错误
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("批量测试完成: " + allDates.size() + " 个日期, 耗时: " + (endTime - startTime) + "ms");

        assertTrue(allPassed, "部分日期加解密测试失败");
    }

    @Test
    public void testSomeDate() throws NoSuchAlgorithmException {
        System.out.println("Min date: " + fpe.getMinDate());
        System.out.println("Max date: " + fpe.getMaxDate());

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

        randomDate.parallelStream().forEach(d -> {
            Date encDateVal = this.fpe.encrypt(d);
            assertTrue(encDateVal != null);
        });
    }

    @Test
    public void decryptDate() {
        for (int i = 0; i < 2; i++) {
            List<DateTime> randomDate = randomDate();

            randomDate.parallelStream().forEach(d -> {
                Date encrypted = this.fpe.encrypt(d);
                Date decrypted = this.fpe.decrypt(encrypted);

                assertTrue(d.equals(decrypted), "original:"+d+", decrypted: "+decrypted+", encrypted: " + encrypted);
            });
        }
    }

    /**
     * 预生成所有有效日期，避免重复的日期验证
     */
    private List<LocalDate> generateAllValidDates(int startYear, int endYear) {
        List<LocalDate> dates = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
                YearMonth yearMonth = YearMonth.of(year, month);
                int daysInMonth = yearMonth.lengthOfMonth();

                for (int day = 1; day <= daysInMonth; day++) {
                    dates.add(LocalDate.of(year, month, day));
                }
            }
        }
        return dates;
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