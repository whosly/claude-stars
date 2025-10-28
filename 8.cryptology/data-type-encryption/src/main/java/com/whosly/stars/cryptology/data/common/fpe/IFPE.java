package com.whosly.stars.cryptology.data.common.fpe;

/**
 * @author fengyang
 * @date 2025-10-28 17:06:33
 */
public interface IFPE<T> {
    /**
     * 加密 T
     */
    T encrypt(T value);

    /**
     * 解密 T
     */
    T decrypt(T encryptedValue);

    /**
     * 批量加密
     */
    T[] encryptBatch(T[] valueList);

    /**
     * 批量解密
     */
    T[] decryptBatch(T[] encryptedValueList);
}
