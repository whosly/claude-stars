package com.whosly.stars.cryptology.data.chars;

import java.util.Arrays;

/**
 * NoPadding 模式下的特殊编解码转换处理
 *
 * @author fengyang
 * @date 2025-10-27 13:34:03
 * @description
 */
public class NoPaddingSmartFixed {
    private static final byte PADDING_MARKER = (byte) 0xAA; // 填充标记

    /**
     * 块大小， 默认为16字节
     */
    private final int blockSize;

    /**
     * 块大小默认为16字节
     */
    public NoPaddingSmartFixed() {
        this(16);
    }

    /**
     * @param blockSize 块大小
     */
    public NoPaddingSmartFixed(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * 智能填充NoPadding模式的数据,通过添加长度信息来区分原始数据和填充数据
     *
     * 1. 自动计算需要填充的长度，确保总长度是SM4块大小(16字节)的倍数
     * 2. 使用特殊标记和长度信息来标识填充边界
     * 3. 通过长度信息来确定原始数据边界
     *
     * @param data 原始数据
     * @return 填充后的数据
     */
    public byte[] smartPadForNoPadding(byte[] data) {
        // 计算需要填充的长度，确保总长度是AES块大小的倍数
        int paddingLength = this.blockSize - (data.length % this.blockSize);

        // 如果原始数据长度已经是块大小的倍数，仍然需要添加一个完整的填充块
        // 这是为了确保解密时能正确识别和移除填充
        if (paddingLength == 0) {
            paddingLength = this.blockSize;
        }
        if (paddingLength < 4) {
            paddingLength = this.blockSize + paddingLength;
        }

        // 创建新的字节数组，长度为原始数据长度+填充长度
        byte[] paddedData = new byte[data.length + paddingLength];

        // 复制原始数据
        System.arraycopy(data, 0, paddedData, 0, data.length);

        // 在填充数据中存储原始数据的长度信息，使用最后4个字节存储原始数据长度
        int originalLength = data.length;
        paddedData[paddedData.length - 4] = (byte) (originalLength & 0xFF);
        paddedData[paddedData.length - 3] = (byte) ((originalLength >> 8) & 0xFF);
        paddedData[paddedData.length - 2] = (byte) ((originalLength >> 16) & 0xFF);
        paddedData[paddedData.length - 1] = (byte) ((originalLength >> 24) & 0xFF);

        return paddedData;
    }

    /**
     * 智能移除NoPadding模式的填充数据,通过读取长度信息来确定原始数据的边界
     *
     * 1. 从填充数据的最后4个字节中读取原始数据长度信息
     * 2. 根据长度信息准确地截取原始数据
     * 3. 验证长度信息的有效性，确保数据完整性
     *
     * @param data 已填充的数据
     * @return 移除填充后的原始数据
     */
    public byte[] smartRemoveNoPadding(byte[] data) {
        // 从填充数据中读取原始数据长度信息，使用最后4个字节存储原始数据长度
        int originalLength = (data[data.length - 1] & 0xFF) << 24 |
                (data[data.length - 2] & 0xFF) << 16 |
                (data[data.length - 3] & 0xFF) << 8 |
                (data[data.length - 4] & 0xFF);

        // 验证长度信息的有效性
        if (originalLength >= 0 && originalLength <= data.length - 4) {
            return Arrays.copyOf(data, originalLength);
        }

        // 如果长度信息无效，返回所有数据（这种情况不应该发生）
        return data;
    }

}
