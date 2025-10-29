package com.whosly.stars.cryptology.data.bigintx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BigIntegerFPE 测试
 */
public class BigIntegerFPETest {

    private TinkBigIntegerFPE bigIntegerFPE;

    @BeforeEach
    public void setUp() throws GeneralSecurityException {
        this.bigIntegerFPE = new TinkBigIntegerFPE();
    }

    @Test
    public void testBasicEncryptionDecryption() {
        // 测试基本加解密功能
        BigInteger value = new BigInteger("12345678901234567890");
        BigInteger domainSize = new BigInteger("100000000000000000000"); // 10^20

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "基本加解密测试失败");
        assertTrue(encrypted.compareTo(BigInteger.ZERO) >= 0, "加密结果应为非负数");
        assertTrue(encrypted.compareTo(domainSize) < 0, "加密结果应在域范围内");
    }

    @Test
    public void testZeroValue() {
        // 测试零值
        BigInteger value = BigInteger.ZERO;
        BigInteger domainSize = new BigInteger("1000");

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "零值加解密测试失败");
    }

    @Test
    public void testOneValue() {
        // 测试1
        BigInteger value = BigInteger.ONE;
        BigInteger domainSize = new BigInteger("1000");

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "1值加解密测试失败");
    }

    @Test
    public void testLargeNumbers() {
        // 测试大数值
        BigInteger value = new BigInteger("1234567890123456789012345678901234567890");
        BigInteger domainSize = new BigInteger("10000000000000000000000000000000000000000");

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "大数值加解密测试失败");
        assertTrue(encrypted.compareTo(domainSize) < 0, "加密结果超出域范围");
    }

    @Test
    public void testPowerOfTwoDomain() {
        // 测试2的幂次方域
        BigInteger value = new BigInteger("12345");
        BigInteger domainSize = BigInteger.ONE.shiftLeft(128); // 2^128

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "2的幂次方域测试失败");
        assertTrue(encrypted.compareTo(domainSize) < 0, "加密结果超出2^128范围");
    }

    @Test
    public void testPrimeDomain() {
        // 测试质数域
        BigInteger value = new BigInteger("123456");
        BigInteger domainSize = new BigInteger("1000003"); // 质数

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "质数域测试失败");
        assertTrue(encrypted.compareTo(domainSize) < 0, "加密结果超出质数域范围");
    }

    @Test
    public void testSmallDomain() {
        // 测试小范围域
        BigInteger value = BigInteger.valueOf(5);
        BigInteger domainSize = BigInteger.valueOf(10);

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "小范围域测试失败");
        assertTrue(encrypted.compareTo(BigInteger.ZERO) >= 0, "加密结果应为非负数");
        assertTrue(encrypted.compareTo(domainSize) < 0, "加密结果应小于10");
    }

    @Test
    public void testVeryLargeDomain() {
        // 测试非常大的域
        BigInteger value = new BigInteger("123456789012345678901234567890");
        BigInteger domainSize = new BigInteger("1000000000000000000000000000000000000000000000000");

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(value, decrypted, "超大域测试失败");
        assertTrue(encrypted.compareTo(domainSize) < 0, "加密结果超出超大域范围");
    }

    @Test
    public void testMultipleRounds() {
        // 测试多次加解密的一致性
        BigInteger value = new BigInteger("98765432109876543210");
        BigInteger domainSize = new BigInteger("100000000000000000000");

        // 多次加密解密，结果应该一致
        BigInteger encrypted1 = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted1 = bigIntegerFPE.decrypt(encrypted1, domainSize);

        BigInteger encrypted2 = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted2 = bigIntegerFPE.decrypt(encrypted2, domainSize);

        assertEquals(value, decrypted1, "第一次加解密失败");
        assertEquals(value, decrypted2, "第二次加解密失败");
        assertEquals(encrypted1, encrypted2, "相同输入应该产生相同输出");
    }

    @Test
    public void testDifferentValuesSameDomain() {
        // 测试同一域内不同值的加解密
        BigInteger domainSize = new BigInteger("1000000");

        BigInteger[] testValues = {
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.valueOf(123),
                BigInteger.valueOf(999999),
                BigInteger.valueOf(500000)
        };

        for (BigInteger value : testValues) {
            BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
            BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

            assertEquals(value, decrypted,
                    String.format("值 %s 加解密测试失败", value));
            assertTrue(encrypted.compareTo(domainSize) < 0,
                    String.format("加密结果 %s 超出域范围 %s", encrypted, domainSize));
        }
    }

    @Test
    public void testValueAtDomainBoundary() {
        // 测试域边界值
        BigInteger domainSize = new BigInteger("1000");
        BigInteger maxValue = domainSize.subtract(BigInteger.ONE); // 999

        BigInteger encrypted = bigIntegerFPE.encrypt(maxValue, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        assertEquals(maxValue, decrypted, "域边界值测试失败");
        assertTrue(encrypted.compareTo(domainSize) < 0, "边界值加密结果超出域范围");
    }

    @Test
    public void testFormatPreservation() {
        // 测试格式保留特性
        BigInteger value = new BigInteger("12345");
        BigInteger domainSize = new BigInteger("100000");

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);

        // 验证加密结果仍在域范围内
        assertTrue(encrypted.compareTo(BigInteger.ZERO) >= 0,
                "加密结果应为非负数");
        assertTrue(encrypted.compareTo(domainSize) < 0,
                "加密结果应在域范围内");

        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);
        assertEquals(value, decrypted, "格式保留测试失败");
    }

    @Test
    public void testNegativeValueThrowsException() {
        // 测试负值应该抛出异常
        BigInteger negativeValue = new BigInteger("-123");
        BigInteger domainSize = new BigInteger("1000");

        assertThrows(IllegalArgumentException.class, () -> {
            bigIntegerFPE.encrypt(negativeValue, domainSize);
        }, "负值加密应该抛出异常");
    }

    @Test
    public void testValueLargerThanDomainThrowsException() {
        // 测试值大于域范围应该抛出异常
        BigInteger value = new BigInteger("1001");
        BigInteger domainSize = new BigInteger("1000");

        assertThrows(IllegalArgumentException.class, () -> {
            bigIntegerFPE.encrypt(value, domainSize);
        }, "值大于域范围应该抛出异常");
    }

    @Test
    public void testZeroDomainSizeThrowsException() {
        // 测试零域大小应该抛出异常
        BigInteger value = BigInteger.ONE;
        BigInteger zeroDomain = BigInteger.ZERO;

        assertThrows(IllegalArgumentException.class, () -> {
            bigIntegerFPE.encrypt(value, zeroDomain);
        }, "零域大小应该抛出异常");
    }

    @Test
    public void testPerformanceWithLargeNumbers() {
        // 性能测试 - 大数值
        BigInteger value = new BigInteger("12345678901234567890123456789012345678901234567890");
        BigInteger domainSize = new BigInteger("100000000000000000000000000000000000000000000000000");

        long startTime = System.currentTimeMillis();

        BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

        long endTime = System.currentTimeMillis();

        assertEquals(value, decrypted, "性能测试加解密失败");
        assertTrue((endTime - startTime) < 5000, "大数值加解密应在5秒内完成"); // 5秒超时
    }

    @Test
    public void testBatchOperations() {
        // 批量操作测试
        BigInteger domainSize = new BigInteger("1000000");
        BigInteger[] values = {
                BigInteger.ZERO,
                BigInteger.valueOf(1),
                BigInteger.valueOf(123456),
                BigInteger.valueOf(999999),
                BigInteger.valueOf(500000)
        };

        for (BigInteger value : values) {
            BigInteger encrypted = bigIntegerFPE.encrypt(value, domainSize);
            BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, domainSize);

            assertEquals(value, decrypted,
                    String.format("批量测试中值 %s 加解密失败", value));
        }
    }

    @Test
    public void testCryptographicProperties() {
        // 测试密码学特性 - 相同输入产生相同输出（确定性加密）
        BigInteger value = new BigInteger("123456789");
        BigInteger domainSize = new BigInteger("1000000000");

        BigInteger encrypted1 = bigIntegerFPE.encrypt(value, domainSize);
        BigInteger encrypted2 = bigIntegerFPE.encrypt(value, domainSize);

        assertEquals(encrypted1, encrypted2, "确定性加密测试失败");

        BigInteger decrypted1 = bigIntegerFPE.decrypt(encrypted1, domainSize);
        BigInteger decrypted2 = bigIntegerFPE.decrypt(encrypted2, domainSize);

        assertEquals(value, decrypted1, "解密一致性测试失败");
        assertEquals(value, decrypted2, "解密一致性测试失败");
    }

    @Test
    public void testIntegrationWithTimestampRange() {
        // 集成测试 - 模拟时间戳范围
        // 时间戳范围：1000-01-01 到 9999-12-31
        BigInteger minTimestamp = new BigInteger("-30610224000000"); // 1000-01-01
        BigInteger maxTimestamp = new BigInteger("253402185599999");  // 9999-12-31
        BigInteger timestampRange = maxTimestamp.subtract(minTimestamp).add(BigInteger.ONE);

        System.out.printf("时间戳范围大小: %s%n", timestampRange);

        // 测试几个时间戳值
        BigInteger[] testTimestamps = {
                minTimestamp,
                minTimestamp.add(BigInteger.ONE),
                maxTimestamp.subtract(BigInteger.ONE),
                maxTimestamp,
                minTimestamp.add(timestampRange.divide(BigInteger.valueOf(2)))
        };

        for (BigInteger timestamp : testTimestamps) {
            BigInteger encrypted = bigIntegerFPE.encrypt(timestamp, timestampRange);
            BigInteger decrypted = bigIntegerFPE.decrypt(encrypted, timestampRange);

            assertEquals(timestamp, decrypted,
                    String.format("时间戳 %s 加解密失败", timestamp));
            assertTrue(encrypted.compareTo(timestampRange) < 0,
                    "加密时间戳超出范围");
        }
    }
}