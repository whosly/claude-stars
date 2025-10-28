package com.whosly.stars.cryptology.data.longx;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fengyang
 * @date 2025-10-27 14:14:33
 * @description
 */
public class LongFPECryptoTest {

    private LongFPECrypto longFPECrypto;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        // 添加异常处理，确保longxFpeCrypto不为null
        this.longFPECrypto = new LongFPECrypto();
        assertNotNull(this.longFPECrypto, "LongxFpeCrypto实例创建失败");
    }

    @Test
    public void testLen1() {
        for (int i = 0; i < 5; i++) {
            for (long source = -9; source <= 9; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen2() {
        for (int i = 0; i < 5; i++) {
            for (long source = -99; source <= 99; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen3() {
        for (int i = 0; i < 5; i++) {
            for (long source = -999; source <= 999; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen4() {
        for (int i = 0; i < 5; i++) {
            List<Long> allVal = new ArrayList<>();
            for (long source = -9999; source <= 9999; source++) {
                allVal.add(source);
            }

            allVal.parallelStream().forEach(val -> {
                long encLongVal = this.longFPECrypto.encrypt(val);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(val, decLongVal);
            });
        }
    }

    @Test
    public void testLen5() {
        List<Long> allVal = new ArrayList<>();
        for (long source = -99999; source <= 99999L; source++) {
            allVal.add(source);
        }

        allVal.parallelStream().forEach(val -> {
            long encLongVal = this.longFPECrypto.encrypt(val);

            long decLongVal = this.longFPECrypto.decrypt(encLongVal);
            assertEquals(val, decLongVal);
        });
    }

    @Test
    public void testFPEEngineBasic() {
        long testValue = 123456789L;

        long encrypted = this.longFPECrypto.encrypt(testValue);
        long decrypted = this.longFPECrypto.decrypt(encrypted);

        System.out.println("FPE引擎测试: " + testValue + " -> " + encrypted + " -> " + decrypted);
        assertEquals(testValue, decrypted, "FPE引擎基本加解密失败");
    }

    @Test
    public void testRandomVal() {
        for (int i = 0; i < 5000; i++) {
            Long source = RandomUtil.randomLong(Long.MIN_VALUE, Long.MAX_VALUE);

            this.longFPECrypto.encrypt(source);
        }
    }

    @Test
    public void testEncrypt() {
        for (int i = 0; i < 5000; i++) {
            Long source = RandomUtil.randomLong(Long.MIN_VALUE, Long.MAX_VALUE);

            this.longFPECrypto.encrypt(source);
        }
    }

    @Test
    public void testDecrypt() {
        for (int i = 0; i < 5000; i++) {
            Long source = RandomUtil.randomLong(Long.MIN_VALUE, Long.MAX_VALUE);

            long encLongVal = this.longFPECrypto.encrypt(source);

            long decLongVal = this.longFPECrypto.decrypt(encLongVal);
            assertEquals(source, decLongVal);
        }
    }
}