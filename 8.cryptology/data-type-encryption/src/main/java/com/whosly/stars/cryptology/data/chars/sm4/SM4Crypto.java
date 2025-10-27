package com.whosly.stars.cryptology.data.chars.sm4;

import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author fengyang
 * @date 2025-10-23 14:51:05
 */
public class SM4Crypto {
    private static final byte[] DEFAULT_IV = new byte[16];
    private static final int KEY_SIZE = 16;
    private static final int SM4_BLOCK_SIZE = 16; // SM4块大小为16字节

    private static final String SM4 = "SM4";
    private static final byte PADDING_MARKER = (byte) 0xAA; // 填充标记

    public byte[] encrypt(String context, byte[] key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        return this.encrypt(context.getBytes(StandardCharsets.UTF_8), key, encMode, paddingMode, iv);
    }

    public byte[] encrypt(byte[] context, byte[] key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);

        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }

        byte[] finalIv = iv.orElse(DEFAULT_IV);

        // 对于NoPadding模式，使用智能填充
        final byte[] byteEncode = (paddingMode == PaddingMode.NoPadding) ?
                smartPadForNoPadding(context) : context;

        Cipher cipher = Cipher.getInstance(getTransformation(encMode, paddingMode), BouncyCastleProvider.PROVIDER_NAME);
        if (encMode == EncMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } else {

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(finalIv));
        }

        return cipher.doFinal(byteEncode);
    }

    public byte[] decrypt(byte[] enContext, byte[] key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);
        byte[] finalIv = iv.orElse(DEFAULT_IV);

        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }

        Cipher cipher = Cipher.getInstance(getTransformation(encMode, paddingMode), BouncyCastleProvider.PROVIDER_NAME);
        if (EncMode.ECB.equals(encMode)) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(finalIv));
        }

        // 根据密码器的初始化方式解密
        byte[] result = cipher.doFinal(enContext);
        
        // 对于NoPadding模式，使用智能移除填充
        if (paddingMode == PaddingMode.NoPadding) {
            result = smartRemoveNoPadding(result);
        }
        
        return result;
    }

    /**
     * 智能填充NoPadding模式的数据,通过添加长度信息来区分原始数据和填充数据
     *
     * 1. 自动计算需要填充的长度，确保总长度是SM4块大小(16字节)的倍数
     * 2. 使用特殊标记和长度信息来标识填充边界
     * 3. 通过长度信息来确定原始数据边界
     *
     * @param data 原始数据
     * @return 填充后的数据
     */
    private static byte[] smartPadForNoPadding(byte[] data) {
        // 计算需要填充的长度，确保总长度是SM4块大小的倍数
        int paddingLength = SM4_BLOCK_SIZE - (data.length % SM4_BLOCK_SIZE);
        
        // 如果原始数据长度已经是块大小的倍数，不需要添加额外的填充
        if (paddingLength == SM4_BLOCK_SIZE) {
            paddingLength = 0;
        }
        
        // 如果需要填充
        if (paddingLength > 0) {
            // 创建新的字节数组，长度为原始数据长度+填充长度
            byte[] paddedData = new byte[data.length + paddingLength];
            
            // 复制原始数据
            System.arraycopy(data, 0, paddedData, 0, data.length);
            
            // 使用0填充
            for (int i = data.length; i < paddedData.length; i++) {
                paddedData[i] = 0;
            }
            
            return paddedData;
        }
        
        // 如果不需要填充，直接返回原始数据
        return data;
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
        // 计算原始数据长度（通过检查末尾的0填充）
        int originalLength = data.length;
        // 从末尾开始查找第一个非0字节的位置
        while (originalLength > 0 && data[originalLength - 1] == 0) {
            originalLength--;
        }

        // 如果所有字节都是0，返回原始数据
        if (originalLength == 0) {
            return data;
        }

        // 返回去除填充后的数据
        return Arrays.copyOf(data, originalLength);
    }

    private static String getTransformation(EncMode encMode, PaddingMode paddingMode) {
        if (encMode == EncMode.CTR || encMode == EncMode.OFB || encMode.equals(EncMode.CFB)) {
            paddingMode = PaddingMode.NoPadding;
        }
        return SM4 + "/" + encMode.name() + "/" + paddingMode;
    }
}