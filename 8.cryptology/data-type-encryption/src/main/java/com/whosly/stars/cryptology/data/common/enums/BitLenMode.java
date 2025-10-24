package com.whosly.stars.cryptology.data.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengyang
 * @date 2025-10-23 11:18:01
 * @description
 */
public enum BitLenMode {
    //56位有点特殊 在DES 中 56位实际上是64位 因为DES只取byte的高7位作为有效加密位
    _56(56),
    _128(128),
    _256(256),
    _512(512),
    _1024(1024),
    _2048(2048),
    _5120(5120);

    private static final Map<Integer, BitLenMode> VALUE_MAP = new HashMap<>();

    static {
        for (BitLenMode mode : BitLenMode.values()) {
            VALUE_MAP.put(mode.value, mode);
        }
    }

    private final int value;
    private final int length;

    BitLenMode(int length) {
        this.length = length;
        this.value = length >> 3;
    }

    public int getValue() {
        return value;
    }

    public int getLength() {
        return length;
    }

    public static BitLenMode fromValue(int value) {
        return VALUE_MAP.get(value);
    }

}
