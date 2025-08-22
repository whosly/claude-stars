# Netty 可视化监控系统

这是一个用于监控和可视化 Netty 应用程序的完整解决方案。

## 模块结构

### netty-monitor-agent
轻量级监控代理库，可以集成到任何 Netty 应用程序中。

**特性：**
- 轻量级，最小依赖
- 自动收集 Channel 信息
- 通过 HTTP 发送监控数据到监控中心
- 支持自定义监控数据

**使用方法：**
```java
// 初始化监控
NettyMonitor.initialize("MyApp");

// 在 ChannelInitializer 中添加监控 Handler
pipeline.addFirst("monitor", NettyMonitor.getMonitorHandler());
```

### console
监控面板，提供 Web 界面来查看和分析监控数据。

**特性：**
- Spring Boot Web 应用
- 实时 WebSocket 数据推送
- HTML5 可视化界面
- RESTful API 接口

**启动方法：**
```bash
cd console
mvn spring-boot:run
```

访问 http://localhost:8080 查看监控面板。

## 快速开始

1. 构建项目：
```bash
mvn clean install
```

2. 启动监控面板：
```bash
cd console
mvn spring-boot:run
```

3. 在你的 Netty 应用中集成监控代理：
```xml
<dependency>
    <groupId>com.yueny.study</groupId>
    <artifactId>netty-monitor-agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
// 在应用启动时初始化
NettyMonitor.initialize("YourAppName");

// 在 ChannelInitializer 中添加监控
pipeline.addFirst("monitor", NettyMonitor.getMonitorHandler());
```

## 架构设计

```
┌─────────────────┐    HTTP POST    ┌─────────────────┐
│  Netty App 1    │ ──────────────> │                 │
│ (monitor-agent) │                 │                 │
└─────────────────┘                 │                 │
                                    │   Console       │
┌─────────────────┐    HTTP POST    │  (Web Panel)    │
│  Netty App 2    │ ──────────────> │                 │
│ (monitor-agent) │                 │                 │
└─────────────────┘                 └─────────────────┘
                                            │
                                            │ WebSocket
                                            ▼
                                    ┌─────────────────┐
                                    │   Web Browser   │
                                    │  (Visualization)│
                                    └─────────────────┘
```

## 示例

查看 `console/src/main/java/com/yueny/stars/netty/visualizer/example/` 目录下的示例代码。