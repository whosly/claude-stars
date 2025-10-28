package com.whosly.stars.cryptology.data.bigintx;

import com.whosly.stars.cryptology.data.common.fpe.Keys;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * 高性能可逆 BigInteger FPE 实现
 */
public class BigIntegerFPE {
    private final byte[] key;
    private final SecureRandom random;
    private final ThreadLocal<MessageDigest> digestThreadLocal;
    private final ThreadLocal<Cipher> cipherThreadLocal;

    // 使用固定的AES密钥长度（128位提高性能）
    private static final int AES_KEY_LENGTH = 16;
    private static final int MAX_ROUNDS = 3;

    public BigIntegerFPE() throws NoSuchAlgorithmException {
        this.key = Arrays.copyOf(Keys.genAesKey(), AES_KEY_LENGTH); // 使用128位密钥
        this.random = new SecureRandom();

        this.digestThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 not available", e);
            }
        });

        this.cipherThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                return Cipher.getInstance("AES/ECB/NoPadding");
            } catch (Exception e) {
                throw new RuntimeException("AES cipher not available", e);
            }
        });
    }

    public BigInteger encrypt(BigInteger value, BigInteger domainSize) {
        validateInput(value, domainSize);

        // 特殊处理小域
        if (domainSize.compareTo(BigInteger.valueOf(2)) <= 0) {
            return value;
        }

        return optimizedFeistelEncrypt(value, domainSize, MAX_ROUNDS);
    }

    public BigInteger decrypt(BigInteger encryptedValue, BigInteger domainSize) {
        validateInput(encryptedValue, domainSize);

        if (domainSize.compareTo(BigInteger.valueOf(2)) <= 0) {
            return encryptedValue;
        }

        return optimizedFeistelDecrypt(encryptedValue, domainSize, MAX_ROUNDS);
    }

    /**
     * 优化的Feistel网络加密
     */
    private BigInteger optimizedFeistelEncrypt(BigInteger value, BigInteger domainSize, int rounds) {
        BigInteger[] domains = balancedDomainSplit(domainSize);
        BigInteger leftSize = domains[0];
        BigInteger rightSize = domains[1];

        BigInteger left = value.divide(rightSize);
        BigInteger right = value.mod(rightSize);

        for (int round = 0; round < rounds; round++) {
            BigInteger temp = left;
            BigInteger fOutput = computeFFunction(right, leftSize, round);
            left = right;
            right = temp.xor(fOutput).mod(leftSize);
        }

        return left.multiply(rightSize).add(right);
    }

    /**
     * 优化的Feistel网络解密
     */
    private BigInteger optimizedFeistelDecrypt(BigInteger value, BigInteger domainSize, int rounds) {
        BigInteger[] domains = balancedDomainSplit(domainSize);
        BigInteger leftSize = domains[0];
        BigInteger rightSize = domains[1];

        BigInteger left = value.divide(rightSize);
        BigInteger right = value.mod(rightSize);

        for (int round = rounds - 1; round >= 0; round--) {
            BigInteger temp = right;
            BigInteger fOutput = computeFFunction(left, leftSize, round);
            right = left;
            left = temp.xor(fOutput).mod(leftSize);
        }

        return left.multiply(rightSize).add(right);
    }

    /**
     * 平衡的域分割算法
     */
    private BigInteger[] balancedDomainSplit(BigInteger domainSize) {
        // 使用近似的平方根分割
        BigInteger sqrt = approximateSqrt(domainSize);
        BigInteger leftSize = sqrt;
        BigInteger rightSize = domainSize.divide(leftSize);

        // 确保分割有效
        while (leftSize.multiply(rightSize).compareTo(domainSize) < 0) {
            leftSize = leftSize.add(BigInteger.ONE);
            rightSize = domainSize.divide(leftSize);

            // 防止无限循环
            if (leftSize.compareTo(domainSize) > 0) {
                leftSize = domainSize.divide(BigInteger.valueOf(2));
                rightSize = BigInteger.valueOf(2);
                break;
            }
        }

        return new BigInteger[]{leftSize, rightSize};
    }

    /**
     * 计算F函数
     */
    private BigInteger computeFFunction(BigInteger input, BigInteger domainSize, int round) {
        try {
            // 生成确定性轮密钥
            byte[] roundKey = generateDeterministicRoundKey(round);
            SecretKeySpec keySpec = new SecretKeySpec(roundKey, "AES");

            Cipher cipher = cipherThreadLocal.get();
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 将输入转换为固定长度字节
            byte[] inputBytes = toFixedLengthBytes(input, 16);
            byte[] encrypted = cipher.doFinal(inputBytes);

            // 转换为BigInteger并映射到域大小
            BigInteger raw = new BigInteger(1, encrypted);
            return raw.mod(domainSize);

        } catch (Exception e) {
            // 降级处理：使用基于哈希的确定性函数
            return fallbackFFunction(input, domainSize, round);
        }
    }

    /**
     * 生成确定性轮密钥
     */
    private byte[] generateDeterministicRoundKey(int round) {
        try {
            MessageDigest digest = digestThreadLocal.get();
            digest.reset();

            ByteBuffer buffer = ByteBuffer.allocate(key.length + 4);
            buffer.put(key);
            buffer.putInt(round);

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
     * 降级F函数（基于哈希）
     */
    private BigInteger fallbackFFunction(BigInteger input, BigInteger domainSize, int round) {
        MessageDigest digest = digestThreadLocal.get();
        digest.reset();

        ByteBuffer buffer = ByteBuffer.allocate(key.length + input.toByteArray().length + 4);
        buffer.put(key);
        buffer.put(input.toByteArray());
        buffer.putInt(round);

        byte[] hash = digest.digest(buffer.array());
        return new BigInteger(1, hash).mod(domainSize);
    }

    /**
     * 固定长度字节转换
     */
    private byte[] toFixedLengthBytes(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();
        byte[] result = new byte[length];

        int srcPos = Math.max(0, bytes.length - length);
        int destPos = Math.max(0, length - bytes.length);
        int copyLength = Math.min(bytes.length, length);

        System.arraycopy(bytes, srcPos, result, destPos, copyLength);
        return result;
    }

    /**
     * 近似平方根计算
     */
    private BigInteger approximateSqrt(BigInteger n) {
        if (n.compareTo(BigInteger.valueOf(10000)) <= 0) {
            long longValue = n.longValue();
            long sqrt = (long) Math.sqrt(longValue);
            return BigInteger.valueOf(sqrt);
        }

        // 对于大数，使用位操作近似
        return BigInteger.ONE.shiftLeft(n.bitLength() / 2);
    }

    /**
     * 输入验证
     */
    private void validateInput(BigInteger value, BigInteger domainSize) {
        if (value == null || domainSize == null) {
            throw new IllegalArgumentException("Value and domain size cannot be null");
        }
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Value must be non-negative: " + value);
        }
        if (domainSize.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Domain size must be positive: " + domainSize);
        }
        if (value.compareTo(domainSize) >= 0) {
            throw new IllegalArgumentException("Value must be less than domain size: " + value + " >= " + domainSize);
        }
    }
}