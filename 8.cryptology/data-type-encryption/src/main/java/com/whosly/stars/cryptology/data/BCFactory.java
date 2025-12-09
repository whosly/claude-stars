package com.whosly.stars.cryptology.data;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * @author fengyang
 * @date 2025-10-29 18:15:20
 * @description
 */
public class BCFactory {
    static {
        registerAlgorithm();
    }

    public static void loading() {

    }

    private static void registerAlgorithm() {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
