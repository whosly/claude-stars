package com.whosly.stars.cryptology.data.datex.impl;

import com.whosly.stars.cryptology.data.datex.ITimestampFPE;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * 时间戳范围 约 280 万亿
 *
 * @author fengyang
 * @date 2025-10-28 15:09:41
 */
class DefaultTimestampFPEImpl extends DefaultDateFPEImpl implements ITimestampFPE {

    private final long minTimestamp;
    private final long maxTimestamp;
    private final long timestampRange;

    public DefaultTimestampFPEImpl(byte[] key) {
        // 使用更安全的日期范围
        this(key,
                Timestamp.valueOf("1000-01-01 00:00:00"),
                Timestamp.valueOf("9999-12-31 23:59:59"));
    }

    public DefaultTimestampFPEImpl(byte[] key, Date minDate, Date maxDate) {
        super(key, minDate, maxDate);

        this.minTimestamp = getMinDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.maxTimestamp = getMaxDate().atTime(23, 59, 59, 999_000_000)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        this.timestampRange = maxTimestamp - minTimestamp + 1;
    }

    public DefaultTimestampFPEImpl(byte[] key, LocalDate minDate, LocalDate maxDate) {
        super(key, minDate, maxDate);

        this.minTimestamp = getMinDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.maxTimestamp = getMaxDate().atTime(23, 59, 59, 999_000_000)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        this.timestampRange = maxTimestamp - minTimestamp + 1;
    }

    @Override
    public Timestamp encrypt(Timestamp timestamp) {
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        // 保存原始纳秒信息
        int originalNanos = timestamp.getNanos();
        long originalTime = timestamp.getTime();

        // 验证输入范围
        if (originalTime < minTimestamp || originalTime > maxTimestamp) {
            throw new IllegalArgumentException(
                    String.format("Input timestamp %d is out of range [%d, %d]",
                            originalTime, minTimestamp, maxTimestamp));
        }

        // 1. 计算相对偏移量 [0, timestampRange]
        long offset = originalTime - minTimestamp;

        // 2. 使用FPE加密偏移量
        long encryptedOffset = this.fpeEngine.encrypt(offset);

        // 3. 将加密结果映射回时间戳范围
        long encryptedTime = minTimestamp + (encryptedOffset % (timestampRange + 1));

        // 4. 创建结果Timestamp
        Timestamp result = new Timestamp(encryptedTime);
        result.setNanos(originalNanos);

        debugOperation("加密", timestamp, result, offset, encryptedOffset);

        return result;
    }

    @Override
    public Timestamp decrypt(Timestamp encryptedTimestamp) {
        Objects.requireNonNull(encryptedTimestamp, "Encrypted date cannot be null");

        // 保存加密时间戳的纳秒信息
        int encryptedNanos = encryptedTimestamp.getNanos();
        long encryptedTime = encryptedTimestamp.getTime();

        // 验证输入范围
        if (encryptedTime < minTimestamp || encryptedTime > maxTimestamp) {
            throw new IllegalArgumentException(
                    String.format("Encrypted timestamp %d is out of range [%d, %d]",
                            encryptedTime, minTimestamp, maxTimestamp));
        }

        // 1. 计算相对偏移量 [0, timestampRange]
        long offset = encryptedTime - minTimestamp;

        // 2. 使用FPE解密偏移量
        long decryptedOffset = this.fpeEngine.decrypt(offset);

        // 3. 将解密结果映射回时间戳范围
        long decryptedTime = minTimestamp + (decryptedOffset % (timestampRange + 1));

        // 4. 创建结果Timestamp
        Timestamp result = new Timestamp(decryptedTime);
        result.setNanos(encryptedNanos);

        debugOperation("解密", encryptedTimestamp, result, offset, decryptedOffset);

        return result;
    }

    @Override
    public Timestamp[] encryptBatch(Timestamp[] timestamps) {
        Objects.requireNonNull(timestamps, "Timestamps array cannot be null");

        Timestamp[] result = new Timestamp[timestamps.length];
        for (int i = 0; i < timestamps.length; i++) {
            result[i] = encrypt(timestamps[i]);
        }
        return result;
    }

    @Override
    public Timestamp[] decryptBatch(Timestamp[] encryptedTimestamps) {
        Objects.requireNonNull(encryptedTimestamps, "Encrypted timestamps array cannot be null");

        Timestamp[] result = new Timestamp[encryptedTimestamps.length];
        for (int i = 0; i < encryptedTimestamps.length; i++) {
            result[i] = decrypt(encryptedTimestamps[i]);
        }
        return result;
    }

    /**
     * 调试信息输出
     */
    private void debugOperation(String operation, Timestamp input, Timestamp output,
                                long offset, long processedOffset) {
        System.out.printf("%s: %s -> %s%n", operation, input, output);
        System.out.printf("  输入时间戳: %,d, 偏移量: %,d, FPE结果: %,d, 最终时间戳: %,d%n",
                input.getTime(), offset, processedOffset, output.getTime());

        // 验证范围
        if (output.getTime() < minTimestamp || output.getTime() > maxTimestamp) {
            System.err.printf("  错误: 输出时间戳 %,d 超出范围 [%,d, %,d]%n",
                    output.getTime(), minTimestamp, maxTimestamp);
        }
    }
}
