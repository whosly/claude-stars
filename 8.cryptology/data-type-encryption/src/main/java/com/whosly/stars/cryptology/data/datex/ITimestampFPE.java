package com.whosly.stars.cryptology.data.datex;

import java.sql.Timestamp;

/**
 * java.sql.Timestamp FPE 算法实现: java.sql.Timestamp   Date 的子类，为数据库设计，增加了纳秒精度。
 *
 * @author fengyang
 * @date 2025-10-28 15:09:01
 */
public interface ITimestampFPE extends IDateFPE {

    /**
     * 加密 Timestamp
     */
    Timestamp encrypt(Timestamp timestamp);

    /**
     * 解密 Timestamp
     */
    Timestamp decrypt(Timestamp encryptedTimestamp);

    Timestamp[] encryptBatch(Timestamp[] timestamps);

    Timestamp[] decryptBatch(Timestamp[] encryptedTimestamps);

}
