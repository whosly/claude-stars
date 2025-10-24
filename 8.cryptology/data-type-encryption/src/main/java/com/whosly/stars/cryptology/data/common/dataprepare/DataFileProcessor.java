package com.whosly.stars.cryptology.data.common.dataprepare;

import java.io.IOException;

/**
 * 数据文件处理器
 * 演示不同的文件读取方法
 */
public class DataFileProcessor {
    
    /**
     * 读取数据文件并分析内容
     * @return 分析结果
     * @throws IOException 读取异常
     */
    public static String analyzeDataFile() throws IOException {
        String content = FileReader.readDataFile();
        
        // 分析文件内容
        int charCount = content.length();
        int lineCount = content.split("\n").length;
        
        StringBuilder result = new StringBuilder();
        result.append("文件分析结果:\n");
        result.append("内容: ").append(content).append("\n");
        result.append("字符数: ").append(charCount).append("\n");
        result.append("行数: ").append(lineCount).append("\n");
        
        return result.toString();
    }
    
    /**
     * 获取文件基本信息
     * @return 基本信息
     */
    public static String getFileBasicInfo() {
        try {
            String content = FileReader.readDataFile();
            return "文件存在，内容长度: " + content.length() + " 字符";
        } catch (IOException e) {
            return "文件读取失败: " + e.getMessage();
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 数据文件基本信息 ===");
            System.out.println(getFileBasicInfo());
            
            System.out.println("\n=== 数据文件详细分析 ===");
            System.out.println(analyzeDataFile());
        } catch (IOException e) {
            System.err.println("处理文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}