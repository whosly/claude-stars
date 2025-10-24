package com.whosly.stars.cryptology.data.common.enums;

/**
 * @author fengyang
 * @date 2025-10-23 11:18:33
 * @description
 */
public enum PaddingMode {
    NoPadding((byte)0),
    PKCS7Padding((byte)1),
    PKCS5Padding((byte)2);

    private final byte value;
    PaddingMode(byte value) {
        this.value = value;
    }
    public byte getValue() {
        return value;
    }
}
