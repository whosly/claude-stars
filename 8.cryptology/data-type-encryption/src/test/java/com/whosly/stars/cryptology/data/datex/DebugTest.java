package com.whosly.stars.cryptology.data.datex;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public class DebugTest {
    
    @Test
    public void testDebug() throws NoSuchAlgorithmException {
        // 配置 FPE
        FPEConfig config = new FPEConfig.Builder()
                .keyFromBase64("MDEyMzQ1Njc4OUFCQ0RFRg==") // 16字节密钥的Base64
                .dateRange(LocalDate.of(2000, 1, 1), LocalDate.of(2030, 12, 31))
                .rounds(10)
                .build();

        ILocalDateFPE fpe = config.createFPE();
        
        System.out.println("Min date: " + fpe.getMinDate());
        System.out.println("Max date: " + fpe.getMaxDate());
        System.out.println("Total days: " + fpe.getTotalDays());
        
        // 计算域分割
        BigInteger[] domains = new BigInteger[]{fpe.getTotalDays().sqrt(), fpe.getTotalDays().divide(fpe.getTotalDays().sqrt())};
        System.out.println("Domain split: " + domains[0] + " x " + domains[1] + " = " + domains[0].multiply(domains[1]));
        System.out.println("Total days: " + fpe.getTotalDays());
        
        // 测试失败的特定日期
        LocalDate testDate = LocalDate.of(2000, 1, 2);
        System.out.println("\n测试日期: " + testDate);
        
        // 计算日期偏移量
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fpe.getMinDate(), testDate);
        System.out.println("日期偏移量: " + daysBetween);
        
        BigInteger dayOffset = BigInteger.valueOf(daysBetween);
        System.out.println("BigInteger日期偏移量: " + dayOffset);
        
        LocalDate encrypted = fpe.encrypt(testDate);
        System.out.println("加密后: " + encrypted);
        
        LocalDate decrypted = fpe.decrypt(encrypted);
        System.out.println("解密后: " + decrypted);
        
        System.out.println("匹配: " + testDate.equals(decrypted));
        
        assert testDate.equals(decrypted) : "加密解密不匹配 for date: " + testDate;
    }
}