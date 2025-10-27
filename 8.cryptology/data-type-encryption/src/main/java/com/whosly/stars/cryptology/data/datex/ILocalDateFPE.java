package com.whosly.stars.cryptology.data.datex;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * LocalDate FPE 算法实现
 *
 * @author fengyang
 * @date 2025-10-27 17:06:01
 * @description
 */
public interface ILocalDateFPE {

    /**
     * 加密 LocalDate
     */
    LocalDate encrypt(LocalDate date);

    /**
     * 解密 LocalDate
     */
    LocalDate decrypt(LocalDate encryptedDate);

    /**
     * 批量加密
     */
    LocalDate[] encryptBatch(LocalDate[] dates);

    /**
     * 批量解密
     */
    LocalDate[] decryptBatch(LocalDate[] encryptedDates);

    LocalDate getMinDate();
    LocalDate getMaxDate();
    int getRounds();
    BigInteger getTotalDays();

    /**
     * 安全擦除密钥
     */
    void secureErase();

}
