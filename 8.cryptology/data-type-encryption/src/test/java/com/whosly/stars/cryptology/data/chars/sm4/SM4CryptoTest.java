package com.whosly.stars.cryptology.data.chars.sm4;

import com.whosly.stars.cryptology.data.common.dataprepare.FileReader;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-10-24 13:57:53
 * @description
 */
public class SM4CryptoTest {

    private static String SM4_KEY = "0123456701234567";
    private static Map<String, String> processResult = new HashMap<>();

    static {
        registerAlgorithm();
    }

    private static void registerAlgorithm() {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private SM4Crypto sm4Crypto = null;
    private String context = "";

    @BeforeEach
    public void setUp() throws IOException {
        this.sm4Crypto = new SM4Crypto();
        this.context = FileReader.readDataFile();

        this.processResult.clear();
    }

    @AfterEach
    public void printResults() {
        System.out.printf("%-15s %-25s %-10s%n", "EncMode", "PaddingMode", "Result");
        System.out.println("--------------------------------------------------------");

        // 按EncMode, PaddingMode排序
        processResult.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String[] parts = entry.getKey().split(":");
                    System.out.printf("%-15s %-25s %-10s%n",
                            parts[0], parts[1], entry.getValue());
                });
        System.out.println("========================================================\n");

        this.processResult.clear();
    }

    @Test
    public void testEncryptStr() {
        System.out.println("\n=== SM4加密 testEncryptStr 测试结果 ===");

        for (EncMode enc : EncMode.values()) {
            for (PaddingMode paddingMode : PaddingMode.values()) {
                // 测试所有padding模式
                try {
                    testEncAndDecWithoutIV(this.context, SM4_KEY.getBytes(), enc, paddingMode);

                    byte[] randomIV = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        randomIV[i] = 0x11;
                    }
                    testEncAndDecWithIV(this.context, SM4_KEY.getBytes(), enc, paddingMode, Optional.of(randomIV));

                    // 记录成功结果
                    String keyWithoutIV = enc + ":" + paddingMode;
                    processResult.put(keyWithoutIV, "PASS");

                    String keyWithIV = enc + ":" + paddingMode + "-WithIV";
                    processResult.put(keyWithIV, "PASS");
                } catch (Exception e) {
                    System.err.println("case error,  enc:" + enc + ", paddingMode:" + paddingMode);
                    e.printStackTrace(); // 打印异常堆栈信息

                    // 记录失败结果
                    String keyWithoutIV = enc + ":" + paddingMode;
                    processResult.put(keyWithoutIV, "FAIL: " + e.getMessage());

                    String keyWithIV = enc + ":" + paddingMode + "-WithIV";
                    processResult.put(keyWithIV, "FAIL: " + e.getMessage());
                    
                    // 重新抛出异常以便测试框架能捕获到
                    fail("Encryption/Decryption failed for enc:" + enc + ", paddingMode:" + paddingMode, e);
                }
            }
        }
    }

    @Test
    public void testEncryptBytes() {
        System.out.println("\n=== SM4加密 testEncryptBytes 测试结果 ===");

        for (EncMode enc : EncMode.values()) {
            for (PaddingMode paddingMode : PaddingMode.values()) {
                // 测试所有padding模式
                try {
                    testEncAndDecBytesWithoutIV(this.context.getBytes(StandardCharsets.UTF_8), SM4_KEY.getBytes(), enc, paddingMode);

                    byte[] randomIV = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        randomIV[i] = 0x11;
                    }
                    testEncAndDecBytesWithIV(this.context.getBytes(StandardCharsets.UTF_8), SM4_KEY.getBytes(), enc, paddingMode, Optional.of(randomIV));

                    // 记录成功结果
                    String keyWithoutIV = enc + ":" + paddingMode;
                    processResult.put(keyWithoutIV, "PASS");

                    String keyWithIV = enc + ":" + paddingMode + "-WithIV";
                    processResult.put(keyWithIV, "PASS");
                } catch (Exception e) {
                    System.err.println("case error,  enc:" + enc + ", paddingMode:" + paddingMode);
                    e.printStackTrace(); // 打印异常堆栈信息

                    // 记录失败结果
                    String keyWithoutIV = enc + ":" + paddingMode;
                    processResult.put(keyWithoutIV, "FAIL: " + e.getMessage());

                    String keyWithIV = enc + ":" + paddingMode + "-WithIV";
                    processResult.put(keyWithIV, "FAIL: " + e.getMessage());

                    // 重新抛出异常以便测试框架能捕获到
                    fail("Encryption/Decryption failed for enc:" + enc + ", paddingMode:" + paddingMode, e);
                }
            }
        }
    }

    @Test
    public void testDecrypt() {
    }

    private void testEncAndDecWithoutIV(String context, byte[] sm4Key, EncMode encMode, PaddingMode paddingMode) throws Exception {
        testEncAndDecWithIV(context, sm4Key, encMode, paddingMode, Optional.empty());
    }

    private void testEncAndDecWithIV(String context, byte[] sm4Key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        byte[] enValue = this.sm4Crypto.encrypt(context, sm4Key, encMode, paddingMode, iv);

        byte[] de_value = this.sm4Crypto.decrypt(enValue, sm4Key, encMode, paddingMode, iv);
        String ming_wen = new String(de_value);
        assertEquals(context, ming_wen, "Decrypted text should match original for enc:" + encMode + ", padding:" + paddingMode);
    }

    private void testEncAndDecBytesWithoutIV(byte[] context, byte[] sm4Key, EncMode encMode, PaddingMode paddingMode) throws Exception {
        testEncAndDecBytesWithIV(context, sm4Key, encMode, paddingMode, Optional.empty());
    }

    private void testEncAndDecBytesWithIV(byte[] context, byte[] sm4Key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        byte[] enValue = this.sm4Crypto.encrypt(context, sm4Key, encMode, paddingMode, iv);

        byte[] de_value = this.sm4Crypto.decrypt(enValue, sm4Key, encMode, paddingMode, iv);
        String ming_wen = new String(de_value, StandardCharsets.UTF_8);
        assertEquals(context, ming_wen, "Decrypted text should match original for enc:" + encMode + ", padding:" + paddingMode);
    }
}