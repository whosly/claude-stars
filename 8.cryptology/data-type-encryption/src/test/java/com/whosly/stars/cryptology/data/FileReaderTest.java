package com.whosly.stars.cryptology.data;

import com.whosly.stars.cryptology.data.common.dataprepare.FileReader;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class FileReaderTest {

    @Test
    void testReadDataFile() throws IOException {
        String content = FileReader.readDataFile();
        assertNotNull(content, "文件内容不应为null");
        assertFalse(content.isEmpty(), "文件内容不应为空");
        assertTrue(content.contains("FPE是一种特殊的加密算法"), "文件内容应包含预期文本");
        System.out.println("文件内容: " + content);
    }

    @Test
    void testReadResourceFile() throws IOException {
        String content = FileReader.readResourceFile("data/enc/data.txt");
        assertNotNull(content, "文件内容不应为null");
        assertFalse(content.isEmpty(), "文件内容不应为空");
        assertTrue(content.contains("FPE是一种特殊的加密算法"), "文件内容应包含预期文本");
        System.out.println("文件内容: " + content);
    }
}