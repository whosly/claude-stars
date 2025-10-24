package com.whosly.stars.cryptology.data.chars.sm4;

import com.whosly.stars.cryptology.data.common.enums.EncMode;
import com.whosly.stars.cryptology.data.common.enums.PaddingMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author fengyang
 * @date 2025-10-23 14:51:05
 * @description
 */
public class SM4Crypto {
    private static final byte[] DEFAULT_IV = new byte[16];
    private static final int KEY_SIZE = 16;

    private static final String SM4 = "SM4";

    public byte[] encrypt(String context, String key, EncMode encMode, PaddingMode paddingMode) throws Exception {
        byte[] byteKey = key.getBytes();

        return encrypt(context, byteKey, encMode, paddingMode);
    }

    public byte[] encrypt(String context, String key, byte[] iv, EncMode encMode, PaddingMode paddingMode) throws Exception {
        byte[] byteKey = key.getBytes();

        return encrypt(context, byteKey, iv, encMode, paddingMode);
    }

    public byte[] encrypt(String context, byte[] key, EncMode encMode, PaddingMode paddingMode) throws Exception {
        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }
        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);
        Cipher cipher = Cipher.getInstance(getTransformation(encMode, paddingMode), BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(context.getBytes(StandardCharsets.UTF_8));

    }

    public byte[] encrypt(String context, byte[] key, byte[] iv, EncMode encMode, PaddingMode paddingMode) throws Exception {

        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);

        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }

        if (iv.length == 0) {
            iv = DEFAULT_IV;
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("向量长度不匹配");
        }

        Cipher cipher = Cipher.getInstance(getTransformation(encMode, paddingMode), BouncyCastleProvider.PROVIDER_NAME);
        if (encMode == EncMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } else {

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        }

        return cipher.doFinal(context.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] decrypt(byte[] enContext, String key, byte[] iv, EncMode encMode, PaddingMode paddingMode) throws Exception {
        byte[] byteKey = key.getBytes();

        return decrypt(enContext, byteKey, iv, encMode, paddingMode);
    }

    public byte[] decrypt(byte[] enContext, String key, EncMode encMode, PaddingMode paddingMode) throws Exception {
        byte[] byteKey = key.getBytes();

        return decrypt(enContext, byteKey, encMode, paddingMode);
    }

    public byte[] decrypt(byte[] enContext, byte[] key, EncMode encMode, PaddingMode paddingMode) throws Exception {
        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }
        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);
        Cipher cipher = Cipher.getInstance(getTransformation(encMode, paddingMode), BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(enContext);
    }

    public byte[] decrypt(byte[] enContext, byte[] key, byte[] iv, EncMode encMode, PaddingMode paddingMode) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, SM4);
        if (iv.length == 0) {
            iv = DEFAULT_IV;
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("向量长度不匹配");
        }

        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥长度不匹配");
        }
        Cipher cipher = Cipher.getInstance(getTransformation(encMode, paddingMode), BouncyCastleProvider.PROVIDER_NAME);
        if (EncMode.ECB.equals(encMode)) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        }

        return cipher.doFinal(enContext);
    }

    private static String getTransformation(EncMode encMode, PaddingMode paddingMode) {
        if (encMode == EncMode.CTR || encMode == EncMode.OFB || encMode.equals(EncMode.CFB)) {
            paddingMode = PaddingMode.NoPadding;
        }
        return SM4 + "/" + encMode.name() + "/" + paddingMode;
    }
}
