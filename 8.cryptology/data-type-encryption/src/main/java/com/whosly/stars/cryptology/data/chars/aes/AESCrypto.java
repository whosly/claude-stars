package com.whosly.stars.cryptology.data.chars.aes;

import com.whosly.stars.cryptology.data.chars.NoPaddingSmartFixed;
import com.whosly.stars.cryptology.data.common.enums.BitLenMode;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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

    private final NoPaddingSmartFixed noPaddingSmartFixed;

    public AESCrypto() {
        this.noPaddingSmartFixed = new NoPaddingSmartFixed(AES_BLOCK_SIZE);
    }

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
                        this.noPaddingSmartFixed.smartPadForNoPadding(context) : context;

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
                result = this.noPaddingSmartFixed.smartRemoveNoPadding(result);
            }
            
            return result;
        });
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