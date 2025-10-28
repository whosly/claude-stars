package com.whosly.stars.cryptology.data.longx;

import com.whosly.stars.cryptology.data.common.fpe.IFPE;
import com.whosly.stars.cryptology.data.common.fpe.Keys;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * Long类型FPE实现: 完整的Feistel网络 - 实现标准的FPE算法， 为自定义实现FPE（Format-Preserving Encryption）算法。 未使用第三方库。
 *
 * @author fengyang
 * @date 2025-10-27 10:13:14
 */
public class LongFPECrypto implements IFPE<Long> {
    private static final BigInteger LONG_MAX = new BigInteger("9223372036854775807");
    private static final BigInteger LONG_MIN = new BigInteger("-9223372036854775808");
    // 2^64
    private static final BigInteger UNSIGNED_LONG_RANGE = new BigInteger("18446744073709551616");

    private final byte[] key;
    private final SecureRandom random;

    public LongFPECrypto() throws NoSuchAlgorithmException {
        this.key = Keys.genAesKey();
        this.random = new SecureRandom();
    }

    @Override
    public Long encrypt(Long value) {
        try {
            // 将有符号Long转换为无符号BigInteger
            BigInteger unsignedValue = longToUnsignedBigInteger(value);

            // 使用优化的Feistel网络加密
            BigInteger encryptedUnsigned = optimizedFeistelEncrypt(unsignedValue, UNSIGNED_LONG_RANGE);

            // 将无符号BigInteger转回有符号Long
            return unsignedBigIntegerToLong(encryptedUnsigned);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed for value: " + value, e);
        }
    }

    @Override
    public Long decrypt(Long encryptedValue) {
        try {
            // 将有符号Long转换为无符号BigInteger
            BigInteger unsignedEncrypted = longToUnsignedBigInteger(encryptedValue);

            // 使用优化的Feistel网络解密
            BigInteger decryptedUnsigned = optimizedFeistelDecrypt(unsignedEncrypted, UNSIGNED_LONG_RANGE);

            // 将无符号BigInteger转回有符号Long
            return unsignedBigIntegerToLong(decryptedUnsigned);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed for value: " + encryptedValue, e);
        }
    }

    @Override
    public Long[] encryptBatch(Long[] valueList) {
        Objects.requireNonNull(valueList, "valueList array cannot be null");

        return Arrays.stream(valueList)
                .parallel()
                .map(this::encrypt)
                .toArray(Long[]::new);
    }

    @Override
    public Long[] decryptBatch(Long[] encryptedValueList) {
        Objects.requireNonNull(encryptedValueList, "Encrypted encryptedValueList array cannot be null");

        return Arrays.stream(encryptedValueList)
                .parallel()
                .map(this::decrypt)
                .toArray(Long[]::new);
    }

    /**
     * 优化的Feistel网络加密
     */
    private BigInteger optimizedFeistelEncrypt(BigInteger value, BigInteger domainSize) {
        int rounds = 4; // 减少轮数

        // 使用固定的域分割，避免复杂的计算
        BigInteger[] domains = fixedDomainSplit(domainSize);
        BigInteger leftSize = domains[0];
        BigInteger rightSize = domains[1];

        // 初始分割
        BigInteger left = value.divide(rightSize);
        BigInteger right = value.mod(rightSize);

        for (int round = 0; round < rounds; round++) {
            // 简化版Feistel轮
            BigInteger temp = left;
            BigInteger fOutput = optimizedFFunction(right, leftSize, round);
            left = right;
            right = temp.xor(fOutput).mod(leftSize);
        }

        // 最终合并
        return left.multiply(rightSize).add(right);
    }

    /**
     * 优化的Feistel网络解密
     */
    private BigInteger optimizedFeistelDecrypt(BigInteger value, BigInteger domainSize) {
        int rounds = 4;

        // 使用与加密相同的固定域分割
        BigInteger[] domains = fixedDomainSplit(domainSize);
        BigInteger leftSize = domains[0];
        BigInteger rightSize = domains[1];

        // 初始分割
        BigInteger left = value.divide(rightSize);
        BigInteger right = value.mod(rightSize);

        for (int round = rounds - 1; round >= 0; round--) {
            // 简化版Feistel轮（解密）
            BigInteger temp = right;
            BigInteger fOutput = optimizedFFunction(left, leftSize, round);
            right = left;
            left = temp.xor(fOutput).mod(leftSize);
        }

        // 最终合并
        return left.multiply(rightSize).add(right);
    }

    /**
     * 固定的域分割 - 避免复杂的计算
     */
    private BigInteger[] fixedDomainSplit(BigInteger domainSize) {
        // 使用固定的分割：左域 = 2^32，右域 = 2^32
        // 这样 leftSize * rightSize = 2^64 = domainSize
        BigInteger leftSize = BigInteger.ONE.shiftLeft(32);  // 2^32
        BigInteger rightSize = BigInteger.ONE.shiftLeft(32); // 2^32
        return new BigInteger[]{leftSize, rightSize};
    }

    /**
     * 优化的F函数
     */
    private BigInteger optimizedFFunction(BigInteger input, BigInteger domainSize, int round) {
        try {
            // 生成简化的轮密钥
            byte[] roundKey = generateSimpleRoundKey(round);
            SecretKeySpec keySpec = new SecretKeySpec(roundKey, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 简化的输入处理：使用long值而不是BigInteger
            long inputLong = input.longValue();
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(0); // 填充前8字节
            buffer.putLong(inputLong); // 后8字节为数据

            byte[] encrypted = cipher.doFinal(buffer.array());

            // 取后8字节作为结果
            ByteBuffer resultBuffer = ByteBuffer.wrap(encrypted);
            resultBuffer.position(8);
            long encryptedLong = resultBuffer.getLong();

            // 映射到域大小
            BigInteger result = BigInteger.valueOf(encryptedLong & 0xFFFFFFFFL); // 取低32位
            return result.mod(domainSize);

        } catch (Exception e) {
            throw new RuntimeException("F function failed for round " + round, e);
        }
    }

    /**
     * 轮密钥生成
     */
    private byte[] generateSimpleRoundKey(int round) {
        try {
            // 使用简单的密钥派生
            ByteBuffer buffer = ByteBuffer.allocate(key.length + 1);
            buffer.put(key);
            buffer.put((byte) round); // 使用byte而不是int

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(buffer.array());

            // 取前16字节作为AES-128密钥
            byte[] roundKey = new byte[16];
            System.arraycopy(hash, 0, roundKey, 0, 16);

            return roundKey;
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed for round " + round, e);
        }
    }

    /**
     * 将有符号Long转换为无符号BigInteger
     */
    private BigInteger longToUnsignedBigInteger(long value) {
        if (value >= 0) {
            return BigInteger.valueOf(value);
        } else {
            // 对于负数，使用2的补码表示
            return BigInteger.valueOf(value).add(UNSIGNED_LONG_RANGE);
        }
    }

    /**
     * 将无符号BigInteger转换为有符号Long
     */
    private long unsignedBigIntegerToLong(BigInteger unsignedValue) {
        // 确保值在无符号Long范围内
        if (unsignedValue.compareTo(UNSIGNED_LONG_RANGE) >= 0) {
            unsignedValue = unsignedValue.mod(UNSIGNED_LONG_RANGE);
        }

        if (unsignedValue.compareTo(LONG_MAX) <= 0) {
            return unsignedValue.longValue();
        } else {
            // 转换回有符号表示
            return unsignedValue.subtract(UNSIGNED_LONG_RANGE).longValue();
        }
    }
}