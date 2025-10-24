package com.whosly.stars.cryptology.data.chars.aes;

import com.whosly.stars.cryptology.data.common.enums.BitLenMode;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author fengyang
 * @date 2025-10-23 11:16:24
 * @description
 */
public class AESCrypto {
    private static final byte[] DEFAULT_IV = new byte[16];
    private static final int AES_BLOCK_SIZE = 16;

    private static final String AES = "AES";

    public byte[] encrypt(String context, byte[] key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        return this.encrypt(context.getBytes(StandardCharsets.UTF_8), key, bitLen, encMode, paddingMode, iv);
    }

    public byte[] encrypt(byte[] context, byte[] key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        byte[] finalIv = iv.orElse(DEFAULT_IV);
        if (finalIv.length != DEFAULT_IV.length) {
            throw new IllegalArgumentException("向量长度不匹配");
        }

        if (bitLen.getValue() != key.length) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }

        // 获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
        final byte[] byteEncode =
                // 对于NoPadding模式，使用智能填充
                (paddingMode == PaddingMode.NoPadding) ?
                        smartPadForNoPadding(context) : context;

        return safeProcess(() -> {
            String transformation = getTransformation(encMode, paddingMode);
            SecretKey secretKey = new SecretKeySpec(key, AES);
            Cipher cipher = javax.crypto.Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            if (encMode == EncMode.ECB) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(finalIv));
            }

            // 根据密码器的初始化方式加密
            return cipher.doFinal(byteEncode);
        });
    }

    public byte[] decrypt(byte[] enContext, byte[] key, BitLenMode bitLen, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        byte[] finalIv = iv.orElse(DEFAULT_IV);
        if (finalIv.length != DEFAULT_IV.length) {
            throw new IllegalArgumentException("向量长度不匹配");
        }

        if (bitLen.getValue() != key.length) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }

        return safeProcess(() -> {
            String transformation = getTransformation(encMode, paddingMode);
            SecretKey secretKey = new SecretKeySpec(key, AES);

            Cipher cipher = javax.crypto.Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            if (encMode == EncMode.ECB) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(finalIv));
            }
            // 根据密码器的初始化方式加密
            byte[] result = cipher.doFinal(enContext);
            
            // 对于NoPadding模式，使用智能移除填充
            if (paddingMode == PaddingMode.NoPadding) {
                result = smartRemoveNoPadding(result);
            }
            
            return result;
        });
    }

    /**
     * 智能填充NoPadding模式的数据,通过添加长度信息来区分原始数据和填充数据
     *
     * 1. 自动计算需要填充的长度，确保总长度是AES块大小(16字节)的倍数
     * 2. 在填充数据的最后4个字节中存储原始数据的长度信息
     * 3. 通过长度信息来确定原始数据边界
     *
     * @param data 原始数据
     * @return 填充后的数据
     */
    private static byte[] smartPadForNoPadding(byte[] data) {
        // 计算需要填充的长度，确保总长度是AES块大小的倍数
        int paddingLength = AES_BLOCK_SIZE - (data.length % AES_BLOCK_SIZE);
        
        // 如果原始数据长度已经是块大小的倍数，仍然需要添加一个完整的填充块
        // 这是为了确保解密时能正确识别和移除填充
        if (paddingLength == 0) {
            paddingLength = AES_BLOCK_SIZE;
        }
        
        // 创建新的字节数组，长度为原始数据长度+填充长度
        byte[] paddedData = new byte[data.length + paddingLength];
        
        // 复制原始数据
        System.arraycopy(data, 0, paddedData, 0, data.length);
        
        // 在填充数据中存储原始数据的长度信息，使用最后4个字节存储原始数据长度
        int originalLength = data.length;
        paddedData[paddedData.length - 4] = (byte) (originalLength & 0xFF);
        paddedData[paddedData.length - 3] = (byte) ((originalLength >> 8) & 0xFF);
        paddedData[paddedData.length - 2] = (byte) ((originalLength >> 16) & 0xFF);
        paddedData[paddedData.length - 1] = (byte) ((originalLength >> 24) & 0xFF);
        
        return paddedData;
    }
    
    /**
     * 智能移除NoPadding模式的填充数据,通过读取长度信息来确定原始数据的边界
     *
     * 1. 从填充数据的最后4个字节中读取原始数据长度信息
     * 2. 根据长度信息准确地截取原始数据
     * 3. 验证长度信息的有效性，确保数据完整性
     *
     * @param data 已填充的数据
     * @return 移除填充后的原始数据
     */
    private static byte[] smartRemoveNoPadding(byte[] data) {
        // 从填充数据中读取原始数据长度信息，使用最后4个字节存储原始数据长度
        int originalLength = (data[data.length - 1] & 0xFF) << 24 |
                            (data[data.length - 2] & 0xFF) << 16 |
                            (data[data.length - 3] & 0xFF) << 8 |
                            (data[data.length - 4] & 0xFF);
        
        // 验证长度信息的有效性
        if (originalLength >= 0 && originalLength <= data.length - 4) {
            return Arrays.copyOf(data, originalLength);
        }
        
        // 如果长度信息无效，返回所有数据（这种情况不应该发生）
        return data;
    }

    private static String getTransformation(EncMode encMode, PaddingMode paddingMode) {
        // 对于流模式(CRT, OFB, CFB)，始终使用NoPadding
        if (EncMode.CTR == encMode || EncMode.OFB == encMode || EncMode.CFB == encMode) {
            return "AES/" + encMode.name() + "/NoPadding";
        }
        
        // 对于PKCS7Padding，使用BouncyCastle支持的名称
        if (PaddingMode.PKCS7Padding == paddingMode) {
            return AES + "/" + encMode.name() + "/PKCS7Padding";
        }
        
        // 其他情况使用标准名称
        return AES + "/" + encMode.name() + "/" + paddingMode;
    }

    private static byte[] safeProcess(Callable<byte[]> callable) throws Exception {
        return callable.call();
    }

}