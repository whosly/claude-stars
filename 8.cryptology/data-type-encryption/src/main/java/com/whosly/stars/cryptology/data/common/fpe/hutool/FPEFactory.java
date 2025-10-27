package com.whosly.stars.cryptology.data.common.fpe.hutool;

import cn.hutool.crypto.symmetric.fpe.FPE;
import org.bouncycastle.crypto.AlphabetMapper;

/**
 * @author fengyang
 * @date 2025-10-27 11:13:34
 * @description
 */
public class FPEFactory {

    /**
     * FF1算法实现
     *
     * @param key aes 密钥，长度必须是16bytes、24bytes或32bytes
     * @param mapper 映射字符表，规定了明文和密文的字符范围
     * @param tweak  Tweak是为了解决因局部加密而导致结果冲突问题，通常情况下将数据的不可变部分作为Tweak，null则使用默认长度全是0的bytes
     * @return
     */
    public static final FPE getFF1(byte[] key, byte[] tweak, AlphabetMapper mapper) {
        return new FPE(FPE.FPEMode.FF1, key, mapper, tweak);
    }

    /**
     * FF3算法实现
     *
     * @param key aes 密钥，长度必须是16bytes、24bytes或32bytes
     * @param mapper 映射字符表，规定了明文和密文的字符范围
     * @param tweak  FF3规定tweak为56bit（即7bytes）
     *
     * @return
     */
    public static final FPE getFF3(byte[] key, byte[] tweak, AlphabetMapper mapper) {
        return new FPE(FPE.FPEMode.FF3_1, key, mapper, tweak);
    }
}
