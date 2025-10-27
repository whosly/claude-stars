package com.whosly.stars.cryptology.data.common.fpe.hutool;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.symmetric.fpe.FPE;
import com.whosly.stars.cryptology.data.common.fpe.CharacterSet;
import com.whosly.stars.cryptology.data.common.fpe.Keys;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * FF1算法实现
 *
 * @author fengyang
 * @date 2025-10-27 11:03:49
 * @description
 */
public class FF1Test extends AbstractFpePrepare {

    @Test
    public void textExample() throws NoSuchAlgorithmException {
        // Tweak是为了解决因局部加密而导致结果冲突问题，通常情况下将数据的不可变部分作为Tweak，null则使用默认长度全是0的bytes
        Map<CharacterSet, FPE> fpeMap =  FPEAlphabetFactory.getFF1(Keys.genAesKey(), null);

        for (int i = 0; i < 20; i++) {
            System.out.println("----------------- ");

            phoneFPE(fpeMap.get(CharacterSet.PHONE_ZH), "135" + RandomUtil.randomNumbers(8));
            creditCardNumberFPE(fpeMap.get(CharacterSet.PHONE_ZH), "123456789" + RandomUtil.randomNumbers(7));
            strFPE(fpeMap.get(CharacterSet.ALPHA_NUMERIC), "38D8DDD0D2" + RandomUtil.randomNumbers(1));
        }
    }
}
