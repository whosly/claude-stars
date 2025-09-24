package com.whosly.stars.java.jvm.stat;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JVM信息工具类， 用于获取和输出当前项目的JVM相关信息
 *
 * @author fengyang
 * @date 2025-09-23 16:23:33
 * @description
 */
public class JVMInfoUtil {
    public static void main(String[] args) {
        printJVMInfo();
    }

    private static final Runtime runtime = Runtime.getRuntime();
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static final long MB = 1024 * 1024;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * 获取JVM内存信息
     */
    public static Map<String, String> getMemoryInfo() {
        Map<String, String> memoryInfo = new LinkedHashMap<>();

        // 堆内存信息
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        memoryInfo.put("堆内存初始大小(MB)", formatBytes(heapMemoryUsage.getInit()));
        memoryInfo.put("堆内存已使用(MB)", formatBytes(heapMemoryUsage.getUsed()));
        memoryInfo.put("堆内存提交大小(MB)", formatBytes(heapMemoryUsage.getCommitted()));
        memoryInfo.put("堆内存最大限制(MB)", formatBytes(heapMemoryUsage.getMax()));
        memoryInfo.put("堆内存使用率", calculateUsage(heapMemoryUsage.getUsed(), heapMemoryUsage.getCommitted()));

        // 非堆内存信息
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        memoryInfo.put("非堆内存初始大小(MB)", formatBytes(nonHeapMemoryUsage.getInit()));
        memoryInfo.put("非堆内存已使用(MB)", formatBytes(nonHeapMemoryUsage.getUsed()));
        memoryInfo.put("非堆内存提交大小(MB)", formatBytes(nonHeapMemoryUsage.getCommitted()));
        memoryInfo.put("非堆内存最大限制(MB)", formatBytes(nonHeapMemoryUsage.getMax()));
        memoryInfo.put("非堆内存使用率", calculateUsage(nonHeapMemoryUsage.getUsed(), nonHeapMemoryUsage.getCommitted()));

        // 运行时内存信息
        memoryInfo.put("JVM总内存(MB)", formatBytes(runtime.totalMemory()));
        memoryInfo.put("JVM空闲内存(MB)", formatBytes(runtime.freeMemory()));
        memoryInfo.put("JVM最大内存(MB)", formatBytes(runtime.maxMemory()));
        memoryInfo.put("可用处理器核心数", String.valueOf(runtime.availableProcessors()));

        return memoryInfo;
    }

    /**
     * 获取JVM运行时信息
     */
    public static Map<String, String> getRuntimeInfo() {
        Map<String, String> runtimeInfo = new LinkedHashMap<>();

        runtimeInfo.put("JVM名称", runtimeMXBean.getVmName());
        runtimeInfo.put("JVM版本", runtimeMXBean.getVmVersion());
        runtimeInfo.put("JVM供应商", runtimeMXBean.getVmVendor());
        runtimeInfo.put("JVM启动时间", formatTimestamp(runtimeMXBean.getStartTime()));
        runtimeInfo.put("JVM运行时间(秒)", String.valueOf(runtimeMXBean.getUptime() / 1000));
        runtimeInfo.put("JVM进程ID", getProcessId());

        // 系统属性
        runtimeInfo.put("Java版本", System.getProperty("java.version"));
        runtimeInfo.put("Java安装目录", System.getProperty("java.home"));
        runtimeInfo.put("Java类路径", System.getProperty("java.class.path"));
        runtimeInfo.put("用户工作目录", System.getProperty("user.dir"));
        runtimeInfo.put("用户主目录", System.getProperty("user.home"));
        runtimeInfo.put("操作系统名称", System.getProperty("os.name"));
        runtimeInfo.put("操作系统架构", System.getProperty("os.arch"));
        runtimeInfo.put("操作系统版本", System.getProperty("os.version"));
        runtimeInfo.put("文件编码", System.getProperty("file.encoding"));

        return runtimeInfo;
    }

    /**
     * 获取线程信息
     */
    public static Map<String, String> getThreadInfo() {
        Map<String, String> threadInfo = new LinkedHashMap<>();

        threadInfo.put("活动线程数", String.valueOf(threadMXBean.getThreadCount()));
        threadInfo.put("峰值线程数", String.valueOf(threadMXBean.getPeakThreadCount()));
        threadInfo.put("守护线程数", String.valueOf(threadMXBean.getDaemonThreadCount()));
        threadInfo.put("启动以来总线程数", String.valueOf(threadMXBean.getTotalStartedThreadCount()));

        return threadInfo;
    }

    /**
     * 获取操作系统信息
     */
    public static Map<String, String> getOSInfo() {
        Map<String, String> osInfo = new LinkedHashMap<>();

        osInfo.put("操作系统名称", osMXBean.getName());
        osInfo.put("操作系统架构", osMXBean.getArch());
        osInfo.put("操作系统版本", osMXBean.getVersion());
        osInfo.put("可用处理器数", String.valueOf(osMXBean.getAvailableProcessors()));

        // 如果支持系统负载
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsMXBean =
                    (com.sun.management.OperatingSystemMXBean) osMXBean;
            osInfo.put("系统负载", String.valueOf(sunOsMXBean.getSystemLoadAverage()));
            osInfo.put("物理内存总量(GB)", formatBytes(sunOsMXBean.getTotalPhysicalMemorySize()));
            osInfo.put("空闲物理内存(GB)", formatBytes(sunOsMXBean.getFreePhysicalMemorySize()));
            osInfo.put("系统内存使用率", calculateUsage(
                    sunOsMXBean.getTotalPhysicalMemorySize() - sunOsMXBean.getFreePhysicalMemorySize(),
                    sunOsMXBean.getTotalPhysicalMemorySize()
            ));
        }

        return osInfo;
    }

    /**
     * 获取所有JVM信息
     */
    public static Map<String, Map<String, String>> getAllJVMInfo() {
        Map<String, Map<String, String>> allInfo = new LinkedHashMap<>();

        allInfo.put("内存信息", getMemoryInfo());
        allInfo.put("运行时信息", getRuntimeInfo());
        allInfo.put("线程信息", getThreadInfo());
        allInfo.put("操作系统信息", getOSInfo());

        return allInfo;
    }

    /**
     * 格式化输出所有JVM信息
     */
    public static void printJVMInfo() {
        Map<String, Map<String, String>> allInfo = getAllJVMInfo();

        System.out.println("=".repeat(80));
        System.out.println("JVM信息报告 - " + new Date());
        System.out.println("=".repeat(80));

        for (Map.Entry<String, Map<String, String>> category : allInfo.entrySet()) {
            System.out.println("\n【" + category.getKey() + "】");
            System.out.println("-".repeat(60));

            Map<String, String> infoMap = category.getValue();
            for (Map.Entry<String, String> entry : infoMap.entrySet()) {
                System.out.printf("%-25s: %s%n", entry.getKey(), entry.getValue());
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    /**
     * 生成HTML格式的JVM信息报告
     */
    public static String generateHTMLReport() {
        StringBuilder html = new StringBuilder();
        Map<String, Map<String, String>> allInfo = getAllJVMInfo();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>JVM信息报告</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        h1 { color: #333; }\n");
        html.append("        .category { margin-bottom: 20px; }\n");
        html.append("        .category h2 { background-color: #f0f0f0; padding: 10px; }\n");
        html.append("        .info-table { width: 100%; border-collapse: collapse; }\n");
        html.append("        .info-table td { padding: 8px; border: 1px solid #ddd; }\n");
        html.append("        .info-table tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>JVM信息报告 - ").append(new Date()).append("</h1>\n");

        for (Map.Entry<String, Map<String, String>> category : allInfo.entrySet()) {
            html.append("    <div class=\"category\">\n");
            html.append("        <h2>").append(category.getKey()).append("</h2>\n");
            html.append("        <table class=\"info-table\">\n");

            Map<String, String> infoMap = category.getValue();
            for (Map.Entry<String, String> entry : infoMap.entrySet()) {
                html.append("            <tr>\n");
                html.append("                <td><strong>").append(entry.getKey()).append("</strong></td>\n");
                html.append("                <td>").append(entry.getValue()).append("</td>\n");
                html.append("            </tr>\n");
            }

            html.append("        </table>\n");
            html.append("    </div>\n");
        }

        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 格式化字节数为MB
     */
    private static String formatBytes(long bytes) {
        if (bytes == -1) return "无限制";
        return df.format(bytes / (double) MB);
    }

    /**
     * 计算使用率
     */
    private static String calculateUsage(long used, long total) {
        if (total <= 0) return "N/A";
        return df.format(used * 100.0 / total) + "%";
    }

    /**
     * 格式化时间戳
     */
    private static String formatTimestamp(long timestamp) {
        return new Date(timestamp).toString();
    }

    /**
     * 获取进程ID
     */
    private static String getProcessId() {
        try {
            String jvmName = runtimeMXBean.getName();
            return jvmName.split("@")[0];
        } catch (Exception e) {
            return "未知";
        }
    }

}
