package com.whosly.stars.cryptology.data.common.enums;

/**
 * @author fengyang
 * @date 2025-10-23 11:17:06
 * @description
 */
public enum EncMode {
    CBC((byte) 1),
    ECB((byte) 2),
    CTR((byte) 3),
    OFB((byte) 4),
    CFB((byte) 5);

    private byte value;

    EncMode(byte value) {
        this.value = value;
    }
    public byte getValue() {
        return value;
    }

}
