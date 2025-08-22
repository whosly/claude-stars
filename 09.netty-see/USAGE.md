# Netty 跨进程监控使用指南

本系统使用 LocalServerChannel 实现同一台机器上不同进程间的 Netty 监控通信。

## 架构说明

```
┌─────────────────────┐    LocalSocket    ┌─────────────────────┐
│   05.chats          │ ──────────────────> │  09.netty-see       │
│  (被监控应用)        │   /tmp/netty-      │   (监控中心)         │
│                     │   monitor.sock     │                     │
│ ┌─────────────────┐ │                    │ ┌─────────────────┐ │
│ │ NettyMonitor    │ │                    │ │ LocalMonitor    │ │
│ │ Agent           │ │                    │ │ Server          │ │
│ └─────────────────┘ │                    │ └─────────────────┘ │
└─────────────────────┘                    └─────────────────────┘
                                                      │
                                                      │ WebSocket
                                                      ▼
                                            ┌─────────────────────┐
                                            │   Web Browser       │
                                            │  (可视化界面)        │
                                            └─────────────────────┘
```

## 使用步骤

### 1. 启动监控中心

```bash
# 进入console目录
cd 09.netty-see/console

# 启动监控面板
mvn spring-boot:run
```

监控面板将在 http://localhost:8080 启动，并创建本地Socket服务器监听 `/tmp/netty-monitor.sock`

### 2. 启动被监控应用

```bash
# 进入chats目录
cd 05.chats

# 启动聊天服务器
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.chats.server.ChatsServer"
```

聊天服务器启动时会：
1. 初始化监控代理
2. 连接到监控中心的LocalSocket
3. 注册应用信息
4. 开始发送Channel监控数据

### 3. 启动聊天客户端

```bash
# 在另一个终端启动客户端
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.chats.client.ChatsClient"
```

### 4. 查看监控数据

打开浏览器访问：http://localhost:8080

- **首页** (`/`) - 监控概览和实时统计
- **Channel详情** (`/channels`) - 详细的Channel信息列表

## 监控数据类型

### 应用注册
```json
{
  "type": "APP_REGISTER",
  "applicationName": "ChatsServer-8080",
  "timestamp": 1692691200000
}
```

### Channel事件
```json
{
  "type": "CHANNEL_ACTIVE",
  "applicationName": "ChatsServer-8080",
  "channelInfo": {
    "channelId": "a1b2c3d4",
    "remoteAddress": "/127.0.0.1:54321",
    "localAddress": "/127.0.0.1:8080",
    "active": true,
    "open": true,
    "writable": true,
    "bytesRead": 0,
    "bytesWritten": 0,
    "timestamp": 1692691200000
  },
  "timestamp": 1692691200000
}
```

## 配置选项

### 监控代理配置

```java
// 使用默认Socket路径
NettyMonitor.initialize("MyApp");

// 自定义Socket路径
NettyMonitor.initialize("MyApp", "/custom/path/monitor.sock");
```

### 监控中心配置

在 `LocalMonitorServer.java` 中修改：

```java
private static final String SOCKET_PATH = "/tmp/netty-monitor.sock";
```

## 故障排除

### 1. Socket连接失败

**问题**：监控代理无法连接到监控中心

**解决方案**：
- 确保监控中心已启动
- 检查Socket文件权限
- 确认Socket路径配置一致

### 2. 监控数据不显示

**问题**：Web界面没有显示监控数据

**解决方案**：
- 检查LocalMonitorServer是否正常启动
- 查看控制台日志
- 确认WebSocket连接正常

### 3. 权限问题

**问题**：无法创建或访问Socket文件

**解决方案**：
```bash
# 检查/tmp目录权限
ls -la /tmp/

# 手动删除旧的socket文件
rm -f /tmp/netty-monitor.sock
```

## 扩展其他应用

要监控其他Netty应用，只需：

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.yueny.study</groupId>
    <artifactId>netty-monitor-agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 初始化监控

```java
// 在应用启动时
NettyMonitor.initialize("YourAppName");
```

### 3. 添加监控Handler

```java
public class YourChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 添加监控Handler（建议放在最前面）
        if (NettyMonitor.isInitialized()) {
            pipeline.addFirst("monitor", NettyMonitor.getMonitorHandler());
        }
        
        // 添加你的业务Handler
        pipeline.addLast(new YourBusinessHandler());
    }
}
```

## 性能说明

- **监控代理**：轻量级设计，对应用性能影响 < 1%
- **本地通信**：使用LocalChannel，比TCP Socket更高效
- **异步处理**：所有监控操作都是异步的，不阻塞业务逻辑
- **故障隔离**：监控失败不会影响主应用运行

## 日志配置

在 `logback.xml` 中配置监控相关日志：

```xml
<!-- 监控代理日志 -->
<logger name="com.yueny.stars.netty.monitor.agent" level="INFO"/>

<!-- 监控服务器日志 -->
<logger name="com.yueny.stars.netty.visualizer.server" level="INFO"/>
```