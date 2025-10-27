package com.whosly.stars.cryptology.data.chars.sm4;

import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;
import java.util.Optional;

public class SM4NoPaddingTest {
    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNoPadding() {
        try {
            SM4Crypto sm4Crypto = new SM4Crypto();
            // 创建一个长度为32字节的测试数据
            String testData = "This is a test message with exactly 32 bytes!!";
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("原始数据: " + testData);
            System.out.println("数据长度: " + testData.getBytes(StandardCharsets.UTF_8).length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = sm4Crypto.encrypt(testData, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = sm4Crypto.decrypt(encrypted, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            String result = new String(decrypted, "utf-8");

            System.out.println("解密结果: " + result);
            System.out.println("解密结果长度: " + result.length() + " 字节");
            System.out.println("是否匹配: " + testData.equals(result));
            
            assertEquals(testData, result, "解密后的数据应与原始数据匹配");
        } catch (Exception e) {
            e.printStackTrace();
            fail("NoPadding模式测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testNoPaddingZero() {
        try {
            SM4Crypto sm4Crypto = new SM4Crypto();
            byte[] testData = new byte[11]; // 11字节数据
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("原始数据: " + Arrays.toString(testData));
            System.out.println("数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = sm4Crypto.encrypt(testData, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = sm4Crypto.decrypt(encrypted, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());

            System.out.println("解密结果: " + Arrays.toString(decrypted));
            System.out.println("解密结果长度: " + decrypted.length + " 字节");
            System.out.println("是否匹配: " + Arrays.equals(testData, decrypted));
            
            assertArrayEquals(testData, decrypted, "NoPadding模式下解密数据应与原始数据匹配");
        } catch (Exception e) {
            e.printStackTrace();
            fail("NoPadding模式测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testNoPaddingWithProperLength() {
        try {
            SM4Crypto sm4Crypto = new SM4Crypto();
            // 创建一个长度为16字节的数组（SM4块大小）
            byte[] testData = new byte[16];
            // 填充一些数据
            for (int i = 0; i < testData.length; i++) {
                testData[i] = (byte) (i % 256);
            }
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("[NoPadding Proper]原始数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = sm4Crypto.encrypt(testData, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("[NoPadding Proper]加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = sm4Crypto.decrypt(encrypted, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            
            System.out.println("[NoPadding Proper]解密结果长度: " + decrypted.length + " 字节");
            System.out.println("[NoPadding Proper]是否匹配: " + Arrays.equals(testData, decrypted));
            
            assertArrayEquals(testData, decrypted, "解密后的数据应与原始数据完全匹配");
        } catch (Exception e) {
            e.printStackTrace();
            fail("NoPadding模式测试失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testNoPaddingWithImproperLength() {
        try {
            SM4Crypto sm4Crypto = new SM4Crypto();
            // 创建一个长度为11字节的数组（不是16的倍数）
            byte[] testData = new byte[11];
            // 填充一些数据
            for (int i = 0; i < testData.length; i++) {
                testData[i] = (byte) (i + 1);
            }
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("[NoPadding Improper]原始数据: " + Arrays.toString(testData));
            System.out.println("[NoPadding Improper]数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式，现在应该能够处理任意长度的数据
            byte[] encrypted = sm4Crypto.encrypt(testData, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("[NoPadding Improper]加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = sm4Crypto.decrypt(encrypted, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            
            System.out.println("[NoPadding Improper]解密结果: " + Arrays.toString(decrypted));
            System.out.println("[NoPadding Improper]解密结果长度: " + decrypted.length + " 字节");
            System.out.println("[NoPadding Improper]是否匹配: " + Arrays.equals(testData, decrypted));
            
            // 验证解密后的数据与原始数据匹配
            assertArrayEquals(testData, decrypted, "解密后的数据应与原始数据完全匹配");
        } catch (Exception e) {
            e.printStackTrace();
            fail("NoPadding模式应该能够处理任意长度的数据: " + e.getMessage());
        }
    }
    
    @Test
    public void testNoPaddingWithVariousModes() {
        try {
            SM4Crypto sm4Crypto = new SM4Crypto();
            // 测试不同的加密模式与NoPadding组合
            byte[] testData = "Test data for SM4 NoPadding with various modes".getBytes();
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥
            byte[] iv = new byte[16]; // 初始化向量
            Arrays.fill(iv, (byte) 0x01); // 填充IV

            System.out.println("[Various Modes]原始数据: " + new String(testData));
            System.out.println("[Various Modes]数据长度: " + testData.length + " 字节");

            // 测试ECB模式
            System.out.println("\n--- ECB Mode ---");
            byte[] encryptedECB = sm4Crypto.encrypt(testData, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            byte[] decryptedECB = sm4Crypto.decrypt(encryptedECB, key, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("[ECB]解密是否匹配: " + Arrays.equals(testData, decryptedECB));
            assertArrayEquals(testData, decryptedECB, "ECB模式下解密数据应与原始数据匹配");

            // 测试CBC模式
            System.out.println("\n--- CBC Mode ---");
            byte[] encryptedCBC = sm4Crypto.encrypt(testData, key, EncMode.CBC, PaddingMode.NoPadding, Optional.of(iv));
            byte[] decryptedCBC = sm4Crypto.decrypt(encryptedCBC, key, EncMode.CBC, PaddingMode.NoPadding, Optional.of(iv));
            System.out.println("[CBC]解密是否匹配: " + Arrays.equals(testData, decryptedCBC));
            assertArrayEquals(testData, decryptedCBC, "CBC模式下解密数据应与原始数据匹配");

            // 测试CTR模式
            System.out.println("\n--- CTR Mode ---");
            byte[] encryptedCTR = sm4Crypto.encrypt(testData, key, EncMode.CTR, PaddingMode.NoPadding, Optional.of(iv));
            byte[] decryptedCTR = sm4Crypto.decrypt(encryptedCTR, key, EncMode.CTR, PaddingMode.NoPadding, Optional.of(iv));
            System.out.println("[CTR]解密是否匹配: " + Arrays.equals(testData, decryptedCTR));
            assertArrayEquals(testData, decryptedCTR, "CTR模式下解密数据应与原始数据匹配");
            
            System.out.println("\n[Various Modes]所有模式测试通过");
        } catch (Exception e) {
            e.printStackTrace();
            fail("各种加密模式下的NoPadding测试失败: " + e.getMessage());
        }
    }
}