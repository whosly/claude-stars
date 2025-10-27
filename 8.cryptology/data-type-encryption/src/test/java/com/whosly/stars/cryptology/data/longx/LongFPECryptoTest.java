package com.whosly.stars.cryptology.data.longx;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

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
            for (long source = 0; source < 10; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen2() {
        for (int i = 0; i < 5; i++) {
            for (long source = 10; source < 100; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen3() {
        for (int i = 0; i < 5; i++) {
            for (long source = 100; source < 1000; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen4() {
        for (int i = 0; i < 5; i++) {
            for (long source = 1000; source < 10000; source++) {
                long encLongVal = this.longFPECrypto.encrypt(source);

                long decLongVal = this.longFPECrypto.decrypt(encLongVal);
                assertEquals(source, decLongVal);
            }
        }
    }

    @Test
    public void testLen5() {
        for (long source = 10000; source < 100000L; source++) {
            long encLongVal = this.longFPECrypto.encrypt(source);

            long decLongVal = this.longFPECrypto.decrypt(encLongVal);
            assertEquals(source, decLongVal);
        }
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