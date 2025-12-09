package com.whosly.stars.cryptology.data;

import com.whosly.stars.cryptology.data.chars.aes.AESCrypto;
import com.whosly.stars.cryptology.data.common.enums.BitLenMode;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import com.whosly.stars.cryptology.data.common.util.HexUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author fengyang
 * @date 2025-10-29 18:03:10
 * @description
 */
public class CryptoTest {
    private static Map<String, String> processResult = new HashMap<>();

    static {
        BCFactory.loading();
    }

    private AESCrypto aesCrypto = null;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        this.aesCrypto = new AESCrypto();

        this.processResult.clear();
    }

    @AfterEach
    public void printResults() {
        System.out.printf("%-15s %-10s %-10s %-15s %-10s%n", "Algo", "BitLenMode", "EncMode", "PaddingMode", "Result");
        System.out.println("--------------------------------------------------------");

        // 按BitLenMode, EncMode, PaddingMode排序
        processResult.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String[] parts = entry.getKey().split(":");
                    System.out.printf("%-15s %-10s %-10s %-15s %-10s%n",
                            parts[0], parts[1], parts[2], parts[3], entry.getValue());
                });
        System.out.println("========================================================\n");

        this.processResult.clear();
    }

    @Test
    public void testCrypto() {
        String context = "基于量子力学原理的不可破解加密技术，确保数据传输的绝对安全。</p>";
        String key16 = "3F987bTdYz12saLx";
        String iv = "Sz6yw60RV95SVNk4";

        // AES
        for (EncMode enc : EncMode.values()) {
            for (PaddingMode paddingMode : PaddingMode.values()) {
                try {
                    byte[] encVal = aesCrypto.encrypt(context, key16.getBytes(), BitLenMode._128, enc, paddingMode, Optional.of(iv.getBytes()));

                    // 记录成功结果
                    String keyWithoutIV = String.format("%s:%s:%s:%s", "AES", BitLenMode._128, enc, paddingMode);
                    processResult.put(keyWithoutIV, "PASS - " + HexUtil.encode(encVal));
                } catch (Exception e) {
                    e.printStackTrace();

                    // 记录失败结果
                    String keyWithoutIV = BitLenMode._128 + ":" + enc + ":" + paddingMode;
                    processResult.put(keyWithoutIV, "FAIL: " + e.getMessage());
                }
            }
        }
    }
}
