package com.whosly.stars.cryptology.data.chars.aes;

import com.whosly.stars.cryptology.data.BCFactory;
import com.whosly.stars.cryptology.data.common.dataprepare.FileReader;
import com.whosly.stars.cryptology.data.common.enums.BitLenMode;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-10-23 11:28:14
 * @description
 */
public class AESCryptoTest {
    private static Map<BitLenMode, String> KEYS = new HashMap<>();
    private static Map<String, String> processResult = new HashMap<>();

    static {
        BCFactory.loading();
    }

    private AESCrypto aesCrypto = null;
    private String context = "";

    @BeforeEach
    public void setUp() throws IOException {
        this.aesCrypto = new AESCrypto();
        this.context = FileReader.readDataFile();

        KEYS.put(BitLenMode._128, "0123456701234567");
        KEYS.put(BitLenMode._256, "01234567012345670123456701234567");

        this.processResult.clear();
    }

    @AfterEach
    public void printResults() {
        System.out.printf("%-15s %-10s %-15s %-10s%n", "BitLenMode", "EncMode", "PaddingMode", "Result");
        System.out.println("--------------------------------------------------------");
        
        // 按BitLenMode, EncMode, PaddingMode排序
        processResult.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String[] parts = entry.getKey().split(":");
                System.out.printf("%-15s %-10s %-15s %-10s%n", 
                    parts[0], parts[1], parts[2], entry.getValue());
            });
        System.out.println("========================================================\n");

        this.processResult.clear();
    }

    @Test
    public void testEncAndDec() {
        System.out.println("\n=== AES加密 testEncAndDec 测试结果 ===");

        for (BitLenMode bitLenMode : BitLenMode.values()) {
            if(!KEYS.containsKey(bitLenMode)) {
                continue;
            }

            String aesKey = KEYS.get(bitLenMode);

            for (EncMode enc : EncMode.values()) {
                for (PaddingMode paddingMode : PaddingMode.values()) {
                    // 测试所有padding模式
                    try {
                        testEncAndDecWithoutIV(this.context, aesKey.getBytes(), bitLenMode, enc, paddingMode);

                        byte[] randomIV = new byte[16];
                        for (int i = 0; i < 16; i++) {
                            randomIV[i] = 0x11;
                        }
                        testEncAndDecWithIV(this.context, aesKey.getBytes(), bitLenMode, enc, paddingMode, Optional.of(randomIV));
                        
                        // 记录成功结果
                        String keyWithoutIV = bitLenMode + ":" + enc + ":" + paddingMode;
                        processResult.put(keyWithoutIV, "PASS");
                        
                        String keyWithIV = bitLenMode + ":" + enc + ":" + paddingMode + ":WithIV";
                        processResult.put(keyWithIV, "PASS");
                    } catch (Exception e) {
                        System.err.println("case error, bitLenMode: " + bitLenMode + ", enc:" + enc + ", paddingMode:" + paddingMode);
                        
                        // 记录失败结果
                        String keyWithoutIV = bitLenMode + ":" + enc + ":" + paddingMode;
                        processResult.put(keyWithoutIV, "FAIL: " + e.getMessage());
                        
                        String keyWithIV = bitLenMode + ":" + enc + ":" + paddingMode + ":WithIV";
                        processResult.put(keyWithIV, "FAIL: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Test
    public void testEncrypt() {
        System.out.println("\n=== AES加密 testEncrypt 测试结果 ===");

        for (BitLenMode bitLenMode : BitLenMode.values()) {
            if(!KEYS.containsKey(bitLenMode)) {
                continue;
            }

            String aesKey = KEYS.get(bitLenMode);

            for (EncMode enc : EncMode.values()) {
                for (PaddingMode paddingMode : PaddingMode.values()) {
                    // 测试所有padding模式
                    try {
                        testEncryptWithoutIV(this.context, aesKey.getBytes(), bitLenMode, enc, paddingMode);

                        byte[] randomIV = new byte[16];
                        for (int i = 0; i < 16; i++) {
                            randomIV[i] = 0x18;
                        }
                        testEncryptWithIV(this.context, aesKey.getBytes(), bitLenMode, enc, paddingMode, Optional.of(randomIV));

                        // 记录成功结果
                        String keyWithoutIV = bitLenMode + ":" + enc + ":" + paddingMode;
                        processResult.put(keyWithoutIV, "PASS");

                        String keyWithIV = bitLenMode + ":" + enc + ":" + paddingMode + ":WithIV";
                        processResult.put(keyWithIV, "PASS");
                    } catch (Exception e) {
                        System.err.println("case error, bitLenMode: " + bitLenMode + ", enc:" + enc + ", paddingMode:" + paddingMode);

                        // 记录失败结果
                        String keyWithoutIV = bitLenMode + ":" + enc + ":" + paddingMode;
                        processResult.put(keyWithoutIV, "FAIL: " + e.getMessage());

                        String keyWithIV = bitLenMode + ":" + enc + ":" + paddingMode + ":WithIV";
                        processResult.put(keyWithIV, "FAIL: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void testEncryptWithoutIV(String context, byte[] aes_key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode) throws Exception {
        testEncryptWithIV(context, aes_key, bitLen, encMode, paddingMode, Optional.empty());
    }

    private void testEncryptWithIV(String context, byte[] aes_key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        byte[] enValue = this.aesCrypto.encrypt(context, aes_key, bitLen, encMode, paddingMode, iv);

        assertTrue(enValue.length > bitLen.getValue());
        assertTrue(enValue.length <= bitLen.getLength()); // CTR 模式加密值长度可能不为 bitLen
    }

    private void testEncAndDecWithoutIV(String context, byte[] aes_key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode) throws Exception {
        testEncAndDecWithIV(context, aes_key, bitLen, encMode, paddingMode, Optional.empty());
    }

    private void testEncAndDecWithIV(String context, byte[] aes_key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        byte[] enValue = this.aesCrypto.encrypt(context, aes_key, bitLen, encMode, paddingMode, iv);

        byte[] de_value = this.aesCrypto.decrypt(enValue, aes_key, bitLen, encMode, paddingMode, iv);
        String ming_wen = new String(de_value);
        assertEquals(context, ming_wen);
    }
}