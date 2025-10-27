package com.whosly.stars.cryptology.data.common.fpe.hutool;

import cn.hutool.crypto.symmetric.fpe.FPE;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FPE经典算法实现。
 *
 * 相比于AES等加密算法，FPE增加BasicAlphabetMapper，即有限字母的字典表。
 *
 * Alphabet：有限字母的字典表，并规定了输出密文的范围，例如对于手机号码而言，是十进制纯数字格式的，其Alphabet包括字符’0′-‘9’。对于MAC地址而言，是十六进制数字格式，其Alphabet应该包括大写英文字母的’A’-‘E’和数字’0’-‘9’在内的十六个字母。
 *
 * @author fengyang
 * @date 2025-10-27 11:18:14
 * @description
 */
abstract class AbstractFpePrepare {
    /**
     * 手机号码
     */
    static void phoneFPE(FPE fpe, String phone) {
        // 加密
        String encrypt = fpe.encrypt(phone);
        System.out.println("加密后的phone: " + encrypt);

        // 解密
        String decrypt = fpe.decrypt(encrypt);
        System.out.println("解密后的phone: " + decrypt);

        assertEquals(phone, decrypt);
    }

    /**
     * 信用卡号
     */
    static void creditCardNumberFPE(FPE fpe, String creditCardNumber) {
        // 加密
        String encrypt = fpe.encrypt(creditCardNumber);
        System.out.println("加密后的信用卡号: " + encrypt);

        // 解密
        String decrypt = fpe.decrypt(encrypt);
        System.out.println("解密后的信用卡号: " + decrypt);

        assertEquals(creditCardNumber, decrypt);
    }

    /**
     * 通用字符串
     */
    static void strFPE(FPE fpe, String str) {
        // 加密
        String encrypt = fpe.encrypt(str);
        System.out.println("加密后的字符串: " + encrypt);

        // 解密
        String decrypt = fpe.decrypt(encrypt);
        System.out.println("解密后的字符串: " + decrypt);

        assertEquals(str, decrypt);
    }
}
