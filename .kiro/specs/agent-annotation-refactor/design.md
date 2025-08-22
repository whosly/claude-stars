# Design Document

## Overview

本设计文档描述了agent模块的注解驱动架构重构。该重构将现有的netty-monitor-agent模块转换为一个轻量级、零依赖（除netty-all外）的监控代理库，支持通过@NettyMonitor注解自动启动监控功能。

核心设计理念：
- **简单易用**：通过注解即可启用监控，无需复杂配置
- **零依赖**：只依赖netty-all，避免依赖冲突
- **自动化**：自动连接管理、重连机制、包扫描
- **轻量级**：内置简单的JSON和日志工具，最小化资源占用

## Architecture

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    User Application                         │
├─────────────────────────────────────────────────────────────┤
│  @NettyMonitor Annotation  │  Manual API  │  Auto Scanning │
├─────────────────────────────────────────────────────────────┤
│              MonitorAnnotationProcessor                     │
├─────────────────────────────────────────────────────────────┤
│                    MonitorAgent                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │  Connection     │  │   Data          │  │   Lifecycle │ │
│  │  Management     │  │   Collection    │  │   Management│ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                    Utility Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │    JsonUtil     │  │     Logger      │  │   Others    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      Netty Core                            │
└─────────────────────────────────────────────────────────────┘
```

### 模块结构

```
agent/
├── src/main/java/com/yueny/stars/netty/monitor/agent/
│   ├── annotation/
│   │   └── NettyMonitor.java                    # 监控注解定义
│   ├── core/
│   │   ├── MonitorAgent.java                    # 监控代理核心类
│   │   ├── MonitorHandler.java                  # 监控数据收集Handler
│   │   └── MonitorBootstrap.java                # 启动引导类（新增）
│   ├── processor/
│   │   ├── MonitorAnnotationProcessor.java      # 注解处理器
│   │   └── PackageScanner.java                  # 包扫描器（新增）
│   ├── model/
│   │   ├── ChannelInfo.java                     # Channel信息模型
│   │   └── MonitorMessage.java                  # 监控消息模型
│   ├── util/
│   │   ├── JsonUtil.java                        # JSON工具类
│   │   ├── Logger.java                          # 日志工具类
│   │   └── ReflectionUtil.java                  # 反射工具类（新增）
│   └── NettyMonitorAgent.java                   # 主入口类（重构）
└── pom.xml
```

## Components and Interfaces

### 1. NettyMonitor注解

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NettyMonitor {
    String value() default "";                    // 应用名称
    String applicationName() default "";          // 应用名称别名
    String host() default "localhost";            // 监控服务器地址
    int port() default 19999;                     // 监控服务器端口
    boolean enabled() default true;               // 是否启用
    int connectTimeout() default 5000;            // 连接超时
    int reconnectInterval() default 5;            // 重连间隔
    boolean autoScan() default false;             // 是否自动扫描包
    String[] scanPackages() default {};           // 扫描的包路径
}
```

### 2. MonitorBootstrap启动引导类

```java
public class MonitorBootstrap {
    // 自动启动方法
    public static void autoStart();
    
    // 手动启动方法
    public static void start(String applicationName);
    public static void start(String applicationName, String host, int port);
    
    // 包扫描启动
    public static void scanAndStart(String... packages);
    
    // 停止方法
    public static void stop();
    
    // 状态查询
    public static boolean isRunning();
}
```

### 3. PackageScanner包扫描器

```java
public class PackageScanner {
    // 扫描指定包中的注解
    public static List<Class<?>> scanForAnnotation(String packageName, Class<? extends Annotation> annotation);
    
    // 扫描当前类路径
    public static List<Class<?>> scanClasspath(Class<? extends Annotation> annotation);
    
    // 处理扫描结果
    public static void processScannedClasses(List<Class<?>> classes);
}
```

### 4. 重构后的MonitorAgent

```java
public class MonitorAgent {
    // 单例模式
    private static volatile MonitorAgent instance;
    
    // 配置信息
    private final MonitorConfig config;
    
    // 连接管理
    private volatile boolean running = false;
    private Channel clientChannel;
    private EventLoopGroup group;
    
    // 生命周期管理
    public static void initialize(MonitorConfig config);
    public static MonitorAgent getInstance();
    public void start();
    public void stop();
    public boolean isRunning();
    
    // 连接管理
    private void connectToServer();
    private void scheduleReconnect();
    private void handleDisconnection();
    
    // 数据发送
    public void sendChannelInfo(ChannelInfo info, String eventType);
    public void sendApplicationInfo();
}
```

### 5. MonitorConfig配置类

```java
public class MonitorConfig {
    private String applicationName;
    private String host;
    private int port;
    private boolean enabled;
    private int connectTimeout;
    private int reconnectInterval;
    
    // 构建器模式
    public static class Builder {
        public Builder applicationName(String name);
        public Builder host(String host);
        public Builder port(int port);
        public Builder enabled(boolean enabled);
        public Builder connectTimeout(int timeout);
        public Builder reconnectInterval(int interval);
        public MonitorConfig build();
    }
}
```

## Data Models

### ChannelInfo扩展

```java
public class ChannelInfo {
    // 基本信息
    private String channelId;
    private String applicationName;        // 新增：应用名称
    private String remoteAddress;
    private String localAddress;
    
    // 状态信息
    private boolean active;
    private boolean open;
    private boolean writable;
    private String state;
    
    // 统计信息
    private long bytesRead;
    private long bytesWritten;
    private long createTime;
    private long lastActiveTime;
    private long timestamp;
    
    // 技术信息
    private String eventLoopGroup;
    private String pipeline;
    private String errorMessage;
    
    // 新增：扩展信息
    private Map<String, Object> attributes;  // 自定义属性
    private String channelType;              // Channel类型
}
```

### MonitorMessage扩展

```java
public class MonitorMessage {
    private String type;                     // 消息类型
    private String applicationName;          // 应用名称
    private long timestamp;                  // 时间戳
    private Object data;                     // 消息数据
    private String version;                  // 协议版本
    private Map<String, Object> metadata;    // 元数据
}
```

## Error Handling

### 1. 连接错误处理

- **连接失败**：记录错误日志，启动重连机制
- **连接超时**：使用配置的超时时间，超时后重连
- **网络中断**：自动检测连接状态，断线重连
- **服务器不可用**：指数退避重连策略

### 2. 注解处理错误

- **类加载失败**：跳过该类，继续处理其他类
- **注解解析错误**：使用默认配置，记录警告日志
- **反射调用失败**：降级处理，不影响主流程

### 3. 数据发送错误

- **序列化失败**：记录错误，跳过该消息
- **网络发送失败**：缓存消息，连接恢复后重发
- **消息过大**：截断或分片处理

### 4. 资源管理错误

- **内存不足**：限制缓存大小，丢弃旧数据
- **线程池满**：使用调用线程执行，记录警告
- **文件系统错误**：禁用文件日志，使用控制台输出

## Testing Strategy

### 1. 单元测试

- **注解处理器测试**：验证注解解析和配置生成
- **包扫描器测试**：验证类发现和过滤逻辑
- **工具类测试**：验证JSON序列化和日志输出
- **配置类测试**：验证配置构建和验证逻辑

### 2. 集成测试

- **连接管理测试**：验证连接建立、重连、关闭流程
- **数据传输测试**：验证监控数据的收集和发送
- **错误恢复测试**：验证各种异常情况的处理
- **生命周期测试**：验证启动、运行、停止流程

### 3. 性能测试

- **内存使用测试**：验证内存占用在合理范围内
- **CPU使用测试**：验证监控开销最小化
- **网络开销测试**：验证数据传输效率
- **并发性能测试**：验证多线程环境下的稳定性

### 4. 兼容性测试

- **Netty版本兼容性**：测试不同Netty版本的兼容性
- **JDK版本兼容性**：测试JDK 8+的兼容性
- **操作系统兼容性**：测试Windows/Linux/Mac的兼容性
- **应用集成测试**：测试与实际应用的集成效果

## Implementation Phases

### Phase 1: 核心重构
- 重构MonitorAgent为单例模式
- 实现MonitorConfig配置类
- 创建MonitorBootstrap启动类
- 完善错误处理机制

### Phase 2: 注解增强
- 增强@NettyMonitor注解功能
- 实现PackageScanner包扫描器
- 完善MonitorAnnotationProcessor
- 添加自动启动机制

### Phase 3: 工具完善
- 增强JsonUtil功能
- 完善Logger日志级别控制
- 实现ReflectionUtil反射工具
- 优化性能和内存使用

### Phase 4: 测试和文档
- 编写完整的单元测试
- 实现集成测试用例
- 编写使用文档和示例
- 性能调优和稳定性测试