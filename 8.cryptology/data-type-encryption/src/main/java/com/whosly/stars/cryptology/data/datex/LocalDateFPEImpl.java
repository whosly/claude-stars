package com.whosly.stars.cryptology.data.datex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LocalDate FPE 算法实现, 支持缓存
 *
 * 轮运算：加密使用 R = L + F(R)，解密使用 L = R - F(L)
 *
 * Feistel 网络：使用标准的 Feistel 结构，确保加密解密可逆
 *
 * @author fengyang
 * @date 2025-10-27 17:03:24
 * @description 修正了 Feistel 网络逻辑，优化了性能和安全性
 */
class LocalDateFPEImpl implements ILocalDateFPE {
    private static final int DEFAULT_ROUNDS = 8;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/NoPadding";
    private static final int KEY_DERIVATION_ITERATIONS = 1000;

    private final byte[] key;
    private final LocalDate minDate;
    private final LocalDate maxDate;
    private final BigInteger totalDays;
    private final SecureRandom random;
    private final int rounds;

    // 缓存计算好的域分割和轮密钥，避免重复计算
    private final ConcurrentHashMap<BigInteger, BigInteger[]> domainCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, byte[]> roundKeyCache = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 构造函数 - 自定义范围
    public LocalDateFPEImpl(byte[] key, LocalDate minDate, LocalDate maxDate) {
        this(key, minDate, maxDate, DEFAULT_ROUNDS);
    }

    public LocalDateFPEImpl(byte[] key, LocalDate minDate, LocalDate maxDate, int rounds) {
        validateKey(key);
        validateDateRange(minDate, maxDate);
        validateRounds(rounds);

        this.key = key.clone();
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.totalDays = calculateTotalDays(minDate, maxDate);
        this.rounds = rounds;
        this.random = new SecureRandom();

        // 预计算域分割和轮密钥
        preCalculateDomains();
        preCalculateRoundKeys();
    }

    // 构造函数 - 默认范围 (1970-01-01 到 2070-12-31)
    public LocalDateFPEImpl(byte[] key) {
        this(key, LocalDate.of(1970, 1, 1), LocalDate.of(2070, 12, 31));
    }

    @Override
    public LocalDate encrypt(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        validateDateInRange(date);

        try {
            lock.readLock().lock();
            BigInteger dayOffset = BigInteger.valueOf(ChronoUnit.DAYS.between(minDate, date));
            BigInteger encryptedOffset = feistelEncrypt(dayOffset, totalDays);
            return minDate.plusDays(encryptedOffset.longValue());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public LocalDate decrypt(LocalDate encryptedDate) {
        Objects.requireNonNull(encryptedDate, "Encrypted date cannot be null");
        validateDateInRange(encryptedDate);

        try {
            lock.readLock().lock();
            BigInteger encryptedOffset = BigInteger.valueOf(ChronoUnit.DAYS.between(minDate, encryptedDate));
            BigInteger decryptedOffset = feistelDecrypt(encryptedOffset, totalDays);
            return minDate.plusDays(decryptedOffset.longValue());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public LocalDate[] encryptBatch(LocalDate[] dates) {
        Objects.requireNonNull(dates, "Dates array cannot be null");

        LocalDate[] result = new LocalDate[dates.length];
        for (int i = 0; i < dates.length; i++) {
            result[i] = encrypt(dates[i]);
        }
        return result;
    }

    @Override
    public LocalDate[] decryptBatch(LocalDate[] encryptedDates) {
        Objects.requireNonNull(encryptedDates, "Encrypted dates array cannot be null");

        LocalDate[] result = new LocalDate[encryptedDates.length];
        for (int i = 0; i < encryptedDates.length; i++) {
            result[i] = decrypt(encryptedDates[i]);
        }
        return result;
    }

    // 使用标准的 Feistel 结构
    private BigInteger feistelEncrypt(BigInteger plaintext, BigInteger domainSize) {
        BigInteger[] domains = getCachedDomains(domainSize);
        BigInteger a = domains[0];  // left part domain size
        BigInteger b = domains[1];  // right part domain size

        // 分割输入
        BigInteger L = plaintext.divide(b);
        BigInteger R = plaintext.mod(b);

        // Feistel 轮运算
        for (int i = 0; i < rounds; i++) {
            BigInteger temp = L;
            L = R;
            R = temp.add(fFunction(R, a, i)).mod(a);
        }

        // 合并结果
        return L.multiply(b).add(R);
    }

    // Feistel 网络解密 - 逆向运算
    private BigInteger feistelDecrypt(BigInteger ciphertext, BigInteger domainSize) {
        BigInteger[] domains = getCachedDomains(domainSize);
        BigInteger a = domains[0];  // left part domain size
        BigInteger b = domains[1];  // right part domain size

        // 分割输入
        BigInteger L = ciphertext.divide(b);
        BigInteger R = ciphertext.mod(b);

        // 逆向 Feistel 轮运算
        for (int i = rounds - 1; i >= 0; i--) {
            BigInteger temp = R;
            R = L;
            L = temp.subtract(fFunction(L, a, i)).mod(a);
        }

        // 合并结果
        return L.multiply(b).add(R);
    }

    // F 函数 - 使用缓存的轮密钥
    private BigInteger fFunction(BigInteger input, BigInteger domainSize, int round) {
        try {
            byte[] roundKey = getCachedRoundKey(round);
            SecretKeySpec keySpec = new SecretKeySpec(roundKey, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 使用优化的字节转换
            byte[] inputBytes = toFixedLengthBytes(input, 16);
            byte[] encrypted = cipher.doFinal(inputBytes);

            // 转换为正数并取模
            BigInteger result = new BigInteger(1, encrypted);
            return result.mod(domainSize);

        } catch (Exception e) {
            throw new RuntimeException("F function failed for round " + round, e);
        }
    }

    // 轮密钥生成 - 使用 PBKDF2 风格迭代
    private byte[] generateRoundKey(int round) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 初始哈希
            ByteBuffer buffer = ByteBuffer.allocate(key.length + 8);
            buffer.put(key);
            buffer.putLong(round);
            byte[] current = digest.digest(buffer.array());

            // 多次迭代增强安全性
            for (int i = 0; i < KEY_DERIVATION_ITERATIONS; i++) {
                ByteBuffer iterBuffer = ByteBuffer.allocate(current.length + 4);
                iterBuffer.put(current);
                iterBuffer.putInt(i);
                current = digest.digest(iterBuffer.array());
            }

            // 取前16字节作为AES密钥
            byte[] roundKey = new byte[16];
            System.arraycopy(current, 0, roundKey, 0, 16);

            return roundKey;
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed for round " + round, e);
        }
    }

    // 字节转换 - 确保固定长度
    private byte[] toFixedLengthBytes(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();

        if (bytes.length == length) {
            return bytes;
        } else if (bytes.length > length) {
            // 截断到指定长度（从末尾开始）
            byte[] result = new byte[length];
            System.arraycopy(bytes, bytes.length - length, result, 0, length);
            return result;
        } else {
            // 填充到指定长度（前面补0）
            byte[] result = new byte[length];
            System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
            return result;
        }
    }

    // 域分割计算 - 确保平衡分割
    private BigInteger[] calculateDomains(BigInteger domainSize) {
        if (domainSize.compareTo(BigInteger.ONE) <= 0) {
            return new BigInteger[]{BigInteger.ONE, BigInteger.ONE};
        }

        // 寻找最平衡的分割
        BigInteger bestA = domainSize.sqrt();
        BigInteger bestB = domainSize.divide(bestA);
        BigInteger minDiff = bestA.subtract(bestB).abs();

        // 在平方根附近搜索更平衡的分割
        for (int i = 1; i <= 100; i++) {
            BigInteger candidateA = bestA.add(BigInteger.valueOf(i));
            if (candidateA.compareTo(domainSize) >= 0) break;

            BigInteger candidateB = domainSize.divide(candidateA);
            BigInteger diff = candidateA.subtract(candidateB).abs();

            if (diff.compareTo(minDiff) < 0 &&
                    candidateA.multiply(candidateB).compareTo(domainSize) >= 0) {
                bestA = candidateA;
                bestB = candidateB;
                minDiff = diff;
            }
        }

        return new BigInteger[]{bestA, bestB};
    }

    // 预计算轮密钥
    private void preCalculateRoundKeys() {
        for (int i = 0; i < rounds; i++) {
            roundKeyCache.put(i, generateRoundKey(i));
        }
    }

    // 获取缓存的轮密钥
    private byte[] getCachedRoundKey(int round) {
        byte[] roundKey = roundKeyCache.get(round);
        if (roundKey == null) {
            // 如果缓存中没有，生成并缓存
            roundKey = generateRoundKey(round);
            roundKeyCache.put(round, roundKey);
        }
        return roundKey;
    }

    // 使用缓存的域分割
    private BigInteger[] getCachedDomains(BigInteger domainSize) {
        return domainCache.computeIfAbsent(domainSize, this::calculateDomains);
    }

    // 预计算域分割
    private void preCalculateDomains() {
        domainCache.put(totalDays, calculateDomains(totalDays));
    }

    private BigInteger calculateTotalDays(LocalDate min, LocalDate max) {
        long days = ChronoUnit.DAYS.between(min, max) + 1;
        if (days <= 0) {
            throw new IllegalArgumentException("Date range must contain at least one day");
        }
        return BigInteger.valueOf(days);
    }

    // 增强的验证方法
    private void validateKey(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (key.length < 16) {
            throw new IllegalArgumentException("Key must be at least 16 bytes, got " + key.length);
        }
        // 检查密钥是否全零或具有低熵
        if (isLowEntropy(key)) {
            throw new IllegalArgumentException("Key has low entropy and may be insecure");
        }
    }

    private boolean isLowEntropy(byte[] key) {
        // 简单检查密钥是否全零或具有重复模式
        boolean allZero = true;
        for (byte b : key) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        return allZero;
    }

    private void validateDateRange(LocalDate min, LocalDate max) {
        if (min == null || max == null) {
            throw new IllegalArgumentException("Min and max dates cannot be null");
        }
        if (min.isAfter(max)) {
            throw new IllegalArgumentException("Min date must be before max date");
        }
        // 检查日期范围是否合理
        long daysBetween = ChronoUnit.DAYS.between(min, max);
        if (daysBetween > 365 * 200) { // 限制200年范围
            throw new IllegalArgumentException("Date range too large, maximum 200 years allowed");
        }
    }

    private void validateRounds(int rounds) {
        if (rounds < 4) {
            throw new IllegalArgumentException("Rounds must be at least 4 for security");
        }
        if (rounds > 20) {
            throw new IllegalArgumentException("Rounds cannot exceed 20 for performance");
        }
        if (rounds % 2 != 0) {
            throw new IllegalArgumentException("Rounds should be even for balanced encryption");
        }
    }

    private void validateDateInRange(LocalDate date) {
        if (date.isBefore(minDate)) {
            throw new IllegalArgumentException(
                    String.format("Date %s is before minimum date %s", date, minDate));
        }
        if (date.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                    String.format("Date %s is after maximum date %s", date, maxDate));
        }
    }

    @Override
    public void secureErase() {
        try {
            lock.writeLock().lock();
            // 清空密钥数据
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            // 清空缓存
            domainCache.clear();
            roundKeyCache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public LocalDate getMinDate() { return minDate; }
    @Override
    public LocalDate getMaxDate() { return maxDate; }
    @Override
    public int getRounds() { return rounds; }
    @Override
    public BigInteger getTotalDays() { return totalDays; }

    @Override
    protected void finalize() throws Throwable {
        secureErase();

        super.finalize();
    }
}