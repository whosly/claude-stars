package com.whosly.stars.cryptology.data.chars.sm4;

import com.whosly.stars.cryptology.data.chars.NoPaddingSmartFixed;
import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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

    private final NoPaddingSmartFixed noPaddingSmartFixed;

    public SM4Crypto() {
        this.noPaddingSmartFixed = new NoPaddingSmartFixed(SM4_BLOCK_SIZE);
    }

    public byte[] encrypt(String context, byte[] key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        return this.encrypt(context.getBytes(StandardCharsets.UTF_8), key, encMode, paddingMode, iv);
    }

    public byte[] encrypt(byte[] context, byte[] key, EncMode encMode, PaddingMode paddingMode, Optional<byte[]> iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);

        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }

        byte[] finalIv = iv.orElse(DEFAULT_IV);

        // 对于NoPadding模式，确保数据长度是块大小的倍数
        final byte[] byteEncode = (paddingMode == PaddingMode.NoPadding) ?
                this.noPaddingSmartFixed.smartPadForNoPadding(context) : context;

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
        
        // 对于NoPadding模式，移除填充
        if (paddingMode == PaddingMode.NoPadding) {
            result = this.noPaddingSmartFixed.smartRemoveNoPadding(result);
        }
        
        return result;
    }

    private static String getTransformation(EncMode encMode, PaddingMode paddingMode) {
        if (encMode == EncMode.CTR || encMode == EncMode.OFB || encMode.equals(EncMode.CFB)) {
            paddingMode = PaddingMode.NoPadding;
        }
        return SM4 + "/" + encMode.name() + "/" + paddingMode;
    }
}