package com.whosly.stars.cryptology.data.datex.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.whosly.stars.cryptology.data.datex.ITimestampFPE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author fengyang
 * @date 2025-10-28 13:25:48
 */
public class TimestampFPEImplTest {

    private ITimestampFPE fpe;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        // 1. 配置 FPE
        FPEConfig config = new FPEConfig.Builder()
                // 16字节密钥的Base64
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==")
                .dateRange(
                        LocalDate.of(1000, 1, 1),
                        LocalDate.of(9999, 12, 31)
                )
                .build();

        this.fpe = config.createTimestampFPE();
    }

    @Test
    public void testTimestampWithNanos() {
        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(
                        LocalDate.of(1000, 1, 1),
                        LocalDate.of(9999, 12, 31)
                )
                .build();

        // 2. 创建 FPE 实例
        ITimestampFPE fpe = config.createTimestampFPE();

        // 创建一个带有纳秒的时间戳
        Timestamp original = Timestamp.valueOf("3870-02-01 18:36:43.000000699");
        System.out.println("原始时间戳: " + original);
        System.out.println("原始纳秒: " + original.getNanos());

        // 加密
        Timestamp encrypted = fpe.encrypt(original);
        System.out.println("加密后时间戳: " + encrypted);
        System.out.println("加密后纳秒: " + encrypted.getNanos());

        // 解密
        Timestamp decrypted = fpe.decrypt(encrypted);
        System.out.println("解密后时间戳: " + decrypted);
        System.out.println("解密后纳秒: " + decrypted.getNanos());

        // 验证纳秒部分是否一致
        assertEquals(original.getNanos(), decrypted.getNanos(),
                "纳秒部分不匹配: 原始=" + original.getNanos() + ", 解密=" + decrypted.getNanos());
    }

    @Test
    public void testVariousNanosValues() {
        byte[] key = "0123456789ABCDEF".getBytes(); // 16字节密钥
        FPEConfig config = new FPEConfig.Builder()
                .key(key)
                .dateRange(
                        LocalDate.of(1000, 1, 1),
                        LocalDate.of(9999, 12, 31)
                )
                .build();

        // 2. 创建 FPE 实例
        ITimestampFPE fpe = config.createTimestampFPE();

        // 测试不同的纳秒值
        int[] nanosValues = {0, 1, 999, 1000, 999999, 1000000, 999999999};

        for (int nanos : nanosValues) {
            Timestamp original = Timestamp.valueOf("2023-06-15 12:30:45.0");
            original.setNanos(nanos);

            Timestamp encrypted = fpe.encrypt(original);
            Timestamp decrypted = fpe.decrypt(encrypted);

            System.out.println("原始: " + original + ", 纳秒: " + original.getNanos());
            System.out.println("解密: " + decrypted + ", 纳秒: " + decrypted.getNanos());

            assertEquals(original.getNanos(), decrypted.getNanos(),
                    "纳秒部分不匹配 for value: " + nanos);
        }
    }

    @Test
    public void testSingle() {
        for (int i = 0; i < 10; i++) {
            FPEConfig config = new FPEConfig.Builder()
                    .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                    .dateRange(
                            DateUtil.parse("1000-01-01"),
                            DateUtil.parse("9999-12-30")
                    )
                    .build();

            // 2. 创建 FPE 实例
            ITimestampFPE fpeSingle = config.createTimestampFPE();

            // 4. 使用加密解密
            Timestamp original = new Timestamp(2080, 11, 21, 12, 13, 55, 0);
            System.out.println("原始日期: " + original);

            Timestamp encrypted = fpeSingle.encrypt(original);
            System.out.println("加密后: " + encrypted);

            Timestamp decrypted = fpeSingle.decrypt(encrypted);
            System.out.println("解密后: " + decrypted);
            System.out.println("匹配: " + original.equals(decrypted));
            assertTrue(original.equals(decrypted), "原始日期:"+original+", 解密后: "+decrypted+", 加密后: " + encrypted);
        }
    }

    @Test
    public void testAllDateRange() {
        boolean printLog = false;

        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(
                        LocalDate.of(1000, 1, 1),
                        LocalDate.of(9999, 12, 31)
                )
                .build();

        // 2. 创建 FPE 实例
        ITimestampFPE fpeSingle = config.createTimestampFPE();

        System.out.println("原始日期        加密后        解密后");
        for (int year = 1970; year <= 2100; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= 31; day++) {
                    Timestamp original = null;
                    try{
                        original = new Timestamp(year, month, day,
                                RandomUtil.randomInt(0, 23), RandomUtil.randomInt(0, 59), RandomUtil.randomInt(0, 59),
                                RandomUtil.randomInt(0, 999));
                    } catch (DateTimeException e) {
                        if(printLog) {
                            System.err.println("错误输入，不是合法的时间：" + String.format("%d-%d-%d", year, month, day));
                        }
                        continue;
                    }

                    Timestamp encrypted = fpeSingle.encrypt(original);

                    Timestamp decrypted = fpeSingle.decrypt(encrypted);
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
        Timestamp[] testDates = {
                toTimestamp(LocalDate.of(2026, 12, 21)),
                toTimestamp(LocalDate.of(2026, 12, 22)),
                toTimestamp(this.fpe.getMinDate().plusDays(2)),
                toTimestamp(this.fpe.getMinDate().plusDays(1)),
                toTimestamp(this.fpe.getMinDate()),
                toTimestamp(this.fpe.getMaxDate().minusDays(5)),
                toTimestamp(this.fpe.getMaxDate().minusDays(4)),
                toTimestamp(this.fpe.getMaxDate().minusDays(3)),
                toTimestamp(this.fpe.getMaxDate().minusDays(2)),
                toTimestamp(this.fpe.getMaxDate().minusDays(1)),
                toTimestamp(this.fpe.getMaxDate()),
                new Timestamp(System.currentTimeMillis()),
                Timestamp.valueOf("2025-10-28 12:00:00"),
                Timestamp.valueOf("1970-01-01 00:00:00"), // 最小边界
                Timestamp.valueOf("2100-12-31 23:59:59"), // 最大边界
        };

        for (Timestamp testDate : testDates) {
            Timestamp encrypted = fpe.encrypt(testDate);
            Timestamp decrypted = fpe.decrypt(encrypted);
            assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
        }
    }

    @Test
    public void testSimple() {
        for (int i = 0; i < 10; i++) {
            Timestamp[] testDates = {
                    toTimestamp(this.fpe.getMinDate().plusDays(2)),
                    toTimestamp(this.fpe.getMinDate().plusDays(1)),
                    toTimestamp(this.fpe.getMinDate())
            };

            for (Timestamp testDate : testDates) {
                Timestamp encrypted = this.fpe.encrypt(testDate);
                Timestamp decrypted = this.fpe.decrypt(encrypted);

                assertTrue(testDate.equals(decrypted), "testDate:"+testDate+", decrypted: "+decrypted+", encrypted: " + encrypted);
            }
        }
    }

    @Test
    public void testEncryptDecrypt() {
        // 测试特定日期
        Timestamp[] testDates = {
                toTimestamp(LocalDate.of(2023, 6, 15)),
                toTimestamp(LocalDate.of(2026, 12, 21)),
                toTimestamp(LocalDate.of(2060, 12, 31)),
                toTimestamp(LocalDate.of(2081, 12, 10)),
                toTimestamp(LocalDate.of(2081, 12, 15)),
                toTimestamp(LocalDate.of(2081, 12, 19)),
                toTimestamp(LocalDate.of(2081, 12, 30)),
                toTimestamp(this.fpe.getMinDate()),
                toTimestamp(this.fpe.getMaxDate()),
                toTimestamp(this.fpe.getMaxDate().minusDays(1)),
                toTimestamp(this.fpe.getMaxDate().minusDays(2)),
                toTimestamp(this.fpe.getMaxDate().minusDays(3))
        };

        for (Timestamp testDate : testDates) {
            try {
                Timestamp encrypted = this.fpe.encrypt(testDate);
                Timestamp decrypted = this.fpe.decrypt(encrypted);
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
            this.fpe.encrypt((Timestamp) null);
        });

        assertThrows(NullPointerException.class, () -> {
            this.fpe.decrypt((Timestamp) null);
        });
    }

    @Test
    public void encrypt() {
        List<Timestamp> randomDate = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            randomDate.add(randomTimestamp());
        }

        for (Timestamp date : randomDate) {
            Timestamp encDateVal = this.fpe.encrypt(date);
            assertTrue(encDateVal != null);
        }
    }

    @Test
    public void decryptDate() {
        List<Timestamp> randomDate = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            randomDate.add(randomTimestamp());
        }

        for (Timestamp original : randomDate) {
            Timestamp encrypted = this.fpe.encrypt(original);
            Timestamp decrypted = this.fpe.decrypt(encrypted);

            assertTrue(original.equals(decrypted), "original:"+original+", decrypted: "+decrypted+", encrypted: " + encrypted);
        }
    }

    /**
     * 生成未来365天内随机一天
     */
    private static Timestamp randomTimestampInFutureYears() {
        // 未来365天内随机一天
        DateTime randomDateTime = RandomUtil.randomDay(0, 365);

        return randomDateTime.toTimestamp();
    }

    private static Timestamp randomTimestamp() {
        // 定义时间范围
        String startStr = "1970-01-01 00:00:00";
        String endStr = "2080-12-31 23:59:59";

        DateTime startTime = DateUtil.parse(startStr);
        DateTime endTime = DateUtil.parse(endStr);

        // 生成范围内的随机时间戳
        long startMillis = startTime.getTime();
        long endMillis = endTime.getTime();
        long randomMillis = RandomUtil.randomLong(startMillis, endMillis);

        Timestamp randomTimestamp = new Timestamp(randomMillis);

        return randomTimestamp;
    }

    /**
     * 生成最近N天的随机时间戳
     * @param days 天数范围
     * @param isFuture true-未来，false-过去
     * @return 随机时间戳
     */
    public static Timestamp generateRecentTimestamp(int days, boolean isFuture) {
        int offset = isFuture ? RandomUtil.randomInt(1, days) : -RandomUtil.randomInt(1, days);
        return DateUtil.offsetDay(DateUtil.date(), offset).toTimestamp();
    }

    /**
     * 生成当前时间附近的随机时间戳（±小时范围）
     * @param hours 小时范围
     * @return 随机时间戳
     */
    public static Timestamp generateNearbyTimestamp(int hours) {
        int offset = RandomUtil.randomInt(-hours, hours);
        return DateUtil.offsetHour(DateUtil.date(), offset).toTimestamp();
    }

    /**
     * 将 LocalDate 转换为当天的开始时间 Timestamp (00:00:00)
     */
    private Timestamp toTimestamp(LocalDate localDate) {
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    /**
     * 将 Timestamp 转换为 LocalDate
     */
    private LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toLocalDate();
    }
}
