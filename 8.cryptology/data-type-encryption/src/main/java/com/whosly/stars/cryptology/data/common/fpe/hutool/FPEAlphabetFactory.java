package com.whosly.stars.cryptology.data.common.fpe.hutool;

import cn.hutool.crypto.symmetric.fpe.FPE;
import com.whosly.stars.cryptology.data.common.fpe.CharacterSet;
import org.bouncycastle.crypto.AlphabetMapper;
import org.bouncycastle.crypto.util.BasicAlphabetMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyang
 * @date 2025-10-27 11:27:00
 * @description
 */
public class FPEAlphabetFactory {

    /**
     * FF1算法实现
     *
     * @param key aes 密钥，长度必须是16bytes、24bytes或32bytes
     * @param tweak  Tweak是为了解决因局部加密而导致结果冲突问题，通常情况下将数据的不可变部分作为Tweak，null则使用默认长度全是0的bytes
     * @return
     */
    public static final Map<CharacterSet, FPE> getFF1(byte[] key, byte[] tweak) {
        Map<CharacterSet, FPE> fpeInstances = new HashMap<>();

        // 预初始化所有字符集的FPE实例
        for (CharacterSet characterSet : CharacterSet.values()) {
            AlphabetMapper mapper = new BasicAlphabetMapper(characterSet.getChars());

            fpeInstances.put(characterSet, FPEFactory.getFF1(key, tweak, mapper));
        }

        return fpeInstances;
    }

    /**
     * FF3算法实现
     *
     * @param key aes 密钥，长度必须是16bytes、24bytes或32bytes
     * @param tweak  FF3规定tweak为56bit（即7bytes）
     *
     * @return
     */
    public static final Map<CharacterSet, FPE> getFF3(byte[] key, byte[] tweak) {
        Map<CharacterSet, FPE> fpeInstances = new HashMap<>();

        // 预初始化所有字符集的FPE实例
        for (CharacterSet characterSet : CharacterSet.values()) {
            AlphabetMapper mapper = new BasicAlphabetMapper(characterSet.getChars());

            fpeInstances.put(characterSet, FPEFactory.getFF3(key, tweak, mapper));
        }

        return fpeInstances;
    }
}
