package com.whosly.stars.cryptology.data.common.fpe;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/**
 * @author fengyang
 * @date 2025-10-27 16:37:51
 * @description
 */
public class Keys {

    /**
     * @return aes 密钥，长度必须是16bytes、24bytes或32bytes
     * @throws NoSuchAlgorithmException
     */
    public static byte[] genAesKey() throws NoSuchAlgorithmException {
        // 初始化 aes 密钥（随机），长度必须是16bytes、24bytes或32bytes
        // 生成一个AES密钥
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        // 128位密钥
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        byte[] key = secretKey.getEncoded();

        return key;
    }
}
