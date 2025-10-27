package com.whosly.stars.cryptology.data.longx;

import com.whosly.stars.cryptology.data.common.fpe.Keys;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Long类型FPE实现: 完整的Feistel网络 - 实现标准的FPE算法， 为自定义实现FPE（Format-Preserving Encryption）算法。 未使用第三方库。
 *
 * @author fengyang
 * @date 2025-10-27 10:13:14
 */
public class LongFPECrypto {
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

    public Long encrypt(Long value) {
        try {
            // 将有符号Long转换为无符号BigInteger
            BigInteger unsignedValue = longToUnsignedBigInteger(value);

            // 使用Feistel网络加密
            BigInteger encryptedUnsigned = feistelEncrypt(unsignedValue, UNSIGNED_LONG_RANGE);

            // 将无符号BigInteger转回有符号Long
            return unsignedBigIntegerToLong(encryptedUnsigned);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed for value: " + value, e);
        }
    }

    public Long decrypt(Long encryptedValue) {
        try {
            // 将有符号Long转换为无符号BigInteger
            BigInteger unsignedEncrypted = longToUnsignedBigInteger(encryptedValue);

            // 使用Feistel网络解密
            BigInteger decryptedUnsigned = feistelDecrypt(unsignedEncrypted, UNSIGNED_LONG_RANGE);

            // 将无符号BigInteger转回有符号Long
            return unsignedBigIntegerToLong(decryptedUnsigned);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed for value: " + encryptedValue, e);
        }
    }

    /**
     * Feistel网络加密
     */
    private BigInteger feistelEncrypt(BigInteger value, BigInteger domainSize) {
        int rounds = 10;
        BigInteger result = value;

        for (int i = 0; i < rounds; i++) {
            result = feistelRound(result, domainSize, i, true);
        }

        return result;
    }

    /**
     * Feistel网络解密
     */
    private BigInteger feistelDecrypt(BigInteger value, BigInteger domainSize) {
        int rounds = 10;
        BigInteger result = value;

        for (int i = rounds - 1; i >= 0; i--) {
            result = feistelRound(result, domainSize, i, false);
        }

        return result;
    }

    /**
     * 自由实现Feistel轮函数
     */
    private BigInteger feistelRound(BigInteger input, BigInteger domainSize, int round, boolean encrypt) {
        // 分割域
        BigInteger[] domains = calculateDomains(domainSize);
        BigInteger leftDomain = domains[0];
        BigInteger rightDomain = domains[1];

        // 分割输入
        BigInteger left = input.divide(rightDomain);
        BigInteger right = input.mod(rightDomain);

        if (encrypt) {
            // Feistel轮运算， 加密轮：L_i+1 = R_i, R_i+1 = L_i ⊕ F(R_i, K_i)
            BigInteger fOutput = fFunction(right, leftDomain, round);
            BigInteger newLeft = right;
            BigInteger newRight = left.xor(fOutput).mod(leftDomain);
            return newLeft.multiply(rightDomain).add(newRight);
        } else {
            // 解密轮的逆运算，解密轮：R_i = L_i+1, L_i = R_i+1 ⊕ F(L_i+1, K_i)
            BigInteger fOutput = fFunction(left, leftDomain, round);
            BigInteger newRight = left;
            BigInteger newLeft = right.xor(fOutput).mod(leftDomain);
            return newLeft.multiply(rightDomain).add(newRight);
        }
    }

    /**
     * F函数 - 使用AES加密
     */
    private BigInteger fFunction(BigInteger input, BigInteger domainSize, int round) {
        try {
            // 生成轮密钥。 使用AES作为伪随机函数，这是FPE的标准做法
            byte[] roundKey = generateRoundKey(round);
            SecretKeySpec keySpec = new SecretKeySpec(roundKey, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 将输入转换为16字节
            byte[] inputBytes = toPaddedBytes(input, 16);
            byte[] encrypted = cipher.doFinal(inputBytes);

            // 转换为BigInteger并映射到域大小。
            // 这是FPE的关键步骤 - 映射到域范围
            BigInteger raw = new BigInteger(1, encrypted);
            return raw.mod(domainSize);

        } catch (Exception e) {
            throw new RuntimeException("F function failed", e);
        }
    }

    /**
     * 生成轮密钥
     */
    private byte[] generateRoundKey(int round) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteBuffer buffer = ByteBuffer.allocate(key.length + 4);
            buffer.put(key);
            buffer.putInt(round);

            byte[] hash = digest.digest(buffer.array());
            // AES-128
            byte[] roundKey = new byte[16];
            System.arraycopy(hash, 0, roundKey, 0, 16);

            return roundKey;
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
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

    /**
     * 计算域分割
     */
    private BigInteger[] calculateDomains(BigInteger domainSize) {
        // 找到最接近平方根的分割点
        BigInteger sqrt = domainSize.sqrt();
        BigInteger leftDomain = sqrt;
        BigInteger rightDomain = domainSize.divide(leftDomain);

        // 确保 leftDomain * rightDomain >= domainSize
        while (leftDomain.multiply(rightDomain).compareTo(domainSize) < 0) {
            leftDomain = leftDomain.add(BigInteger.ONE);
            rightDomain = domainSize.divide(leftDomain);
        }

        return new BigInteger[]{leftDomain, rightDomain};
    }

    /**
     * 填充字节数组
     */
    private byte[] toPaddedBytes(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();
        byte[] result = new byte[length];

        int srcPos = Math.max(0, bytes.length - length);
        int destPos = Math.max(0, length - bytes.length);
        int copyLength = Math.min(bytes.length, length);

        System.arraycopy(bytes, srcPos, result, destPos, copyLength);
        return result;
    }
}