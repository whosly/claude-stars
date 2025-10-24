package com.whosly.stars.cryptology.data.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author fengyang
 * @date 2025-10-23 11:19:39
 * @description
 */
public class HexUtilTest {

    @Test
    public void testHex() throws Exception {
        String data = "列混淆子层是AES算法中最为复杂的部分，属于扩散层，列混淆操作是AES算法中主要的扩散元素，它混淆了输入矩阵的每一列，使输入的每个字节都会影响到4个输出字节。行位移子层和列混淆子层的组合使得经过三轮处理以后,矩阵的每个字节都依赖于16个明文字节成可能。其中包含了矩阵乘法、伽罗瓦域内加法和乘法的相关知识。";

        byte[] dataBytes = data.getBytes();
        String hex = HexUtil.encode(dataBytes);
        System.out.println(hex);
        Assertions.assertEquals(hex, "e58897e6b7b7e6b786e5ad90e5b182e698af414553e7ae97e6b395e4b8ade69c80e4b8bae5a48de69d82e79a84e983a8e58886efbc8ce5b19ee4ba8ee689a9e695a3e5b182efbc8ce58897e6b7b7e6b786e6938de4bd9ce698af414553e7ae97e6b395e4b8ade4b8bbe8a681e79a84e689a9e695a3e58583e7b4a0efbc8ce5ae83e6b7b7e6b786e4ba86e8be93e585a5e79fa9e998b5e79a84e6af8fe4b880e58897efbc8ce4bdbfe8be93e585a5e79a84e6af8fe4b8aae5ad97e88a82e983bde4bc9ae5bdb1e5938de588b034e4b8aae8be93e587bae5ad97e88a82e38082e8a18ce4bd8de7a7bbe5ad90e5b182e5928ce58897e6b7b7e6b786e5ad90e5b182e79a84e7bb84e59088e4bdbfe5be97e7bb8fe8bf87e4b889e8bdaee5a484e79086e4bba5e5908e2ce79fa9e998b5e79a84e6af8fe4b8aae5ad97e88a82e983bde4be9de8b596e4ba8e3136e4b8aae6988ee69687e5ad97e88a82e68890e58fafe883bde38082e585b6e4b8ade58c85e590abe4ba86e79fa9e998b5e4b998e6b395e38081e4bcbde7bd97e793a6e59f9fe58685e58aa0e6b395e5928ce4b998e6b395e79a84e79bb8e585b3e79fa5e8af86e38082");

        byte[] value = HexUtil.decode(hex);
        Assertions.assertEquals(data, new String(value));
    }
}
