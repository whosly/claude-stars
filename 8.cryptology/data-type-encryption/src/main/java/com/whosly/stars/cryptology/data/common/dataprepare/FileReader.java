package com.whosly.stars.cryptology.data.common.dataprepare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 文件读取工具类
 */
public class FileReader {
    
    /**
     * 读取资源文件内容
     * @param fileName 文件名
     * @return 文件内容
     * @throws IOException 读取异常
     */
    public static String readResourceFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (InputStream inputStream = FileReader.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        // 删除最后的换行符
        if (content.length() > 0) {
            content.deleteCharAt(content.length() - 1);
        }
        
        return content.toString();
    }
    
    /**
     * 读取data.txt文件内容
     * @return 文件内容
     * @throws IOException 读取异常
     */
    public static String readDataFile() throws IOException {
        return readResourceFile("data/enc/data.txt");
    }
}