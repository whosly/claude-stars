package com.whosly.stars.cryptology.data.common.fpe.hutool;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.symmetric.fpe.FPE;
import com.whosly.stars.cryptology.data.common.fpe.CharacterSet;
import com.whosly.stars.cryptology.data.common.fpe.Keys;
import org.bouncycastle.crypto.util.BasicAlphabetMapper;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

/**
 * @author fengyang
 * @date 2025-10-27 11:13:09
 * @description
 */
public class FF3Test extends AbstractFpePrepare {

    private static FPE fpe = null;

    @Test
    public void textExample() throws NoSuchAlgorithmException {
        // 映射字符表，规定了明文和密文的字符范围
        BasicAlphabetMapper numberMapper = new BasicAlphabetMapper(CharacterSet.PHONE_ZH.getChars());

        fpe = FPEFactory.getFF3(Keys.genAesKey(),
                // 此处FF3规定tweak为56bit（即7bytes）
                new byte[7], numberMapper);

        for (int i = 0; i < 20; i++) {
            System.out.println("----------------- ");

            phoneFPE(fpe, "135" + RandomUtil.randomNumbers(8));
            creditCardNumberFPE(fpe, "123456789" + RandomUtil.randomNumbers(7));
        }
    }
}
