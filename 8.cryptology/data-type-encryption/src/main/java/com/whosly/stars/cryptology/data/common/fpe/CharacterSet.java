package com.whosly.stars.cryptology.data.common.fpe;

/**
 * 字符集定义
 *
 * @author fengyang
 * @date 2025-10-27 11:01:06
 * @description
 */
public enum CharacterSet {
    /**
     * Long 最大长度 19位
     */
    NUMERIC("0123456789", 1, 20),

    DATE("0123456789-", 10, 10),

    PHONE_ZH("0123456789", 11, 11),
    ALPHA_NUMERIC("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", 1, 50),
    LOWER_ALPHA_NUMERIC("0123456789abcdefghijklmnopqrstuvwxyz", 1, 50);

    private final String chars;
    private final char[] characters;
    private final int minLength;
    private final int maxLength;

    CharacterSet(String chars, int minLength, int maxLength) {
        this.chars = chars;
        this.characters = chars.toCharArray();
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public String getChars() {
        return chars;
    }

    public char[] getCharacters() {
        return characters;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void validateLength(String text) {
        if (text.length() < minLength || text.length() > maxLength) {
            throw new IllegalArgumentException(
                    String.format("Text length must be between %d and %d for character set %s",
                            minLength, maxLength, this.name()));
        }
    }
}
