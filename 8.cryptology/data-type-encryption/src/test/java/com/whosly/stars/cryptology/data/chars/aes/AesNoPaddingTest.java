package com.whosly.stars.cryptology.data.chars.aes;

import com.whosly.stars.cryptology.data.common.enums.BitLenMode;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.security.Security;
import java.util.Arrays;
import java.util.Optional;

public class AesNoPaddingTest {
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
            AESCrypto aesCrypto = new AESCrypto();
            String testData = "This is a test message with exactly 32 bytes!!";
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("原始数据: " + testData);
            System.out.println("数据长度: " + testData.getBytes().length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = aesCrypto.encrypt(testData, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = aesCrypto.decrypt(encrypted, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            String result = new String(decrypted);

            System.out.println("解密结果: " + result);
            System.out.println("解密结果长度: " + result.length() + " 字节");
            System.out.println("是否匹配: " + testData.equals(result.trim()));
        } catch (Exception e) {
            e.printStackTrace();

            assertFalse(true, e.getMessage());
        }
    }

    @Test
    public void testNoPaddingZero() {
        try {
            AESCrypto aesCrypto = new AESCrypto();
            byte[] testData = new byte[11];
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("原始数据: " + new String(testData));
            System.out.println("数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = aesCrypto.encrypt(testData, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = aesCrypto.decrypt(encrypted, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            String result = new String(decrypted);
            System.out.println("原始数据: " + testData);

            System.out.println("解密结果: " + result);
            System.out.println("解密结果长度: " + result.length() + " 字节");
            System.out.println("是否匹配: " + testData.equals(result.trim()));
        } catch (Exception e) {
            e.printStackTrace();

            assertFalse(true, e.getMessage());
        }
    }

    @Test
    public void testNoPaddingZeroExt() {
        for (PaddingMode pad : PaddingMode.values()) {
            try {
                AESCrypto aesCrypto = new AESCrypto();
                byte[] testData = new byte[11];
                // 填充一些数据，包括0值
                for (int i = 0; i < testData.length; i++) {
                    testData[i] = (byte) (i % 256);
                }
                byte[] key = "0123456701234567".getBytes(); // 16字节密钥

                System.out.println("[" + pad + "]原始数据: " + Arrays.toString(testData));
                System.out.println("[" + pad + "]数据长度: " + testData.length + " 字节");

                // 测试NoPadding模式
                byte[] encrypted = aesCrypto.encrypt(testData, key, BitLenMode._128, EncMode.ECB, pad, Optional.empty());
                System.out.println("[" + pad + "]加密后长度: " + encrypted.length + " 字节");

                byte[] decrypted = aesCrypto.decrypt(encrypted, key, BitLenMode._128, EncMode.ECB, pad, Optional.empty());

                System.out.println("[" + pad + "]解密结果: " + Arrays.toString(decrypted));
                System.out.println("[" + pad + "]解密结果长度: " + decrypted.length + " 字节");
                System.out.println("[" + pad + "]是否匹配: " + Arrays.equals(testData, decrypted));

                // 对于NoPadding模式，应该能够正确解密
                if (pad == PaddingMode.NoPadding) {
                    assertArrayEquals(testData, decrypted, "NoPadding模式下解密数据应与原始数据匹配");
                }
            } catch (Exception e) {
                e.printStackTrace();
                assertFalse(true, e.getMessage());
            }
        }
    }

    @Test
    public void testNoPaddingWithProperLength() {
        try {
            AESCrypto aesCrypto = new AESCrypto();
            // 创建一个长度为16字节的数组（AES块大小）
            byte[] testData = new byte[16];
            // 填充一些数据，包括0值
            for (int i = 0; i < testData.length; i++) {
                testData[i] = (byte) (i % 256);
            }
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("[NoPadding Proper]原始数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = aesCrypto.encrypt(testData, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("[NoPadding Proper]加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = aesCrypto.decrypt(encrypted, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            
            System.out.println("[NoPadding Proper]解密结果长度: " + decrypted.length + " 字节");
            System.out.println("[NoPadding Proper]是否匹配: " + Arrays.equals(testData, decrypted));
            
            assertArrayEquals(testData, decrypted, "解密后的数据应与原始数据完全匹配");
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true, e.getMessage());
        }
    }
    
    @Test
    public void testNoPaddingWithImproperLength() {
        try {
            AESCrypto aesCrypto = new AESCrypto();
            // 创建一个长度为11字节的数组（不是16的倍数）
            byte[] testData = new byte[11];
            // 填充一些数据
            for (int i = 0; i < testData.length; i++) {
                testData[i] = (byte) (i % 256);
            }
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("[NoPadding Improper]原始数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式，现在应该能够处理任意长度的数据
            byte[] encrypted = aesCrypto.encrypt(testData, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("[NoPadding Improper]加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = aesCrypto.decrypt(encrypted, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            
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
    public void testOriginalNoPaddingIssue() {
        try {
            AESCrypto aesCrypto = new AESCrypto();
            // 创建一个长度为11字节的数组，模拟原始问题
            byte[] testData = new byte[11];
            byte[] key = "0123456701234567".getBytes(); // 16字节密钥

            System.out.println("[Original Issue]原始数据: " + Arrays.toString(testData));
            System.out.println("[Original Issue]数据长度: " + testData.length + " 字节");

            // 测试NoPadding模式
            byte[] encrypted = aesCrypto.encrypt(testData, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            System.out.println("[Original Issue]加密后长度: " + encrypted.length + " 字节");

            byte[] decrypted = aesCrypto.decrypt(encrypted, key, BitLenMode._128, EncMode.ECB, PaddingMode.NoPadding, Optional.empty());
            
            System.out.println("[Original Issue]解密结果: " + Arrays.toString(decrypted));
            System.out.println("[Original Issue]解密结果长度: " + decrypted.length + " 字节");
            System.out.println("[Original Issue]是否匹配: " + Arrays.equals(testData, decrypted));
            
            // 验证解密后的数据与原始数据匹配
            assertArrayEquals(testData, decrypted, "解密后的数据应与原始数据完全匹配");
            System.out.println("[Original Issue]测试通过：NoPadding模式可以正确处理全零字节数组");
        } catch (Exception e) {
            e.printStackTrace();
            fail("NoPadding模式处理全零字节数组时出错: " + e.getMessage());
        }
    }
}