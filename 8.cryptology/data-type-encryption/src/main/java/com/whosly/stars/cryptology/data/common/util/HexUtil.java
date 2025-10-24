package com.whosly.stars.cryptology.data.common.util;

import java.util.Arrays;

/**
 * @author fengyang
 * @date 2025-10-23 11:19:00
 * @description
 */
public class HexUtil {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final byte[] HEX_DECODE_TABLE = new byte[128];

    public HexUtil() {
    }

    public static String encode(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for(int i = 0; i < bytes.length; ++i) {
            int v = bytes[i] & 255;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 15];
        }

        return new String(hexChars);
    }

    public static byte[] decode(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException("Null input");
        } else if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Odd length hex string");
        } else {
            char[] chars = hexString.toCharArray();
            byte[] bytes = new byte[chars.length / 2];

            for(int i = 0; i < bytes.length; ++i) {
                int high = getHexValue(chars[i * 2], hexString);
                int low = getHexValue(chars[i * 2 + 1], hexString);
                bytes[i] = (byte)(high << 4 | low);
            }

            return bytes;
        }
    }

    private static int getHexValue(char c, String source) {
        if (c >= HEX_DECODE_TABLE.length) {
            throw new IllegalArgumentException("Invalid hex char '" + c + "' in: " + source);
        } else {
            int value = HEX_DECODE_TABLE[c];
            if (value == -1) {
                throw new IllegalArgumentException("Invalid hex char '" + c + "' in: " + source);
            } else {
                return value;
            }
        }
    }

    static {
        Arrays.fill(HEX_DECODE_TABLE, (byte)-1);

        for(int i = 0; i < 10; ++i) {
            HEX_DECODE_TABLE[48 + i] = (byte)i;
            HEX_DECODE_TABLE[97 + i] = (byte)(10 + i);
            HEX_DECODE_TABLE[65 + i] = (byte)(10 + i);
        }

    }
}
