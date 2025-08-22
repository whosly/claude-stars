# Netty Monitor Agent

轻量级 Netty 监控代理库，用于收集和发送 Netty 应用程序的监控数据。

## 特性

- **轻量级**：最小依赖，不影响应用性能
- **自动监控**：自动收集 Channel 生命周期信息
- **数据上报**：通过 HTTP 发送监控数据到监控中心
- **易于集成**：只需几行代码即可集成

## 快速开始

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
// 在应用启动时初始化监控
NettyMonitor.initialize("MyNettyApp");
```

### 3. 添加监控Handler

```java
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 添加监控Handler（建议放在pipeline的最前面）
        if (NettyMonitor.isInitialized()) {
            pipeline.addFirst("monitor", NettyMonitor.getMonitorHandler());
        }
        
        // 添加你的其他Handler
        pipeline.addLast(new MyBusinessHandler());
    }
}
```

## API 文档

### NettyMonitor

主要的监控工具类。

#### 方法

- `initialize(String appName)` - 初始化监控，指定应用名称
- `initialize(String appName, String monitorUrl)` - 初始化监控，指定应用名称和监控中心URL
- `isInitialized()` - 检查监控是否已初始化
- `getMonitorHandler()` - 获取监控Handler实例
- `shutdown()` - 关闭监控

#### 示例

```java
// 基本初始化（默认发送到 http://localhost:8080）
NettyMonitor.initialize("MyApp");

// 自定义监控中心地址
NettyMonitor.initialize("MyApp", "http://monitor.example.com:8080");

// 检查是否已初始化
if (NettyMonitor.isInitialized()) {
    // 添加监控Handler
    pipeline.addFirst("monitor", NettyMonitor.getMonitorHandler());
}

// 应用关闭时清理资源
NettyMonitor.shutdown();
```

## 监控数据

监控代理会自动收集以下信息：

- **Channel信息**：ID、远程地址、本地地址、状态
- **生命周期事件**：连接建立、连接断开
- **时间戳**：事件发生时间

## 配置

### 默认配置

- 监控中心URL：`http://localhost:8080`
- 数据发送端点：`/api/monitor/data`
- 连接超时：5秒
- 读取超时：10秒

### 自定义配置

```java
// 自定义监控中心地址
NettyMonitor.initialize("MyApp", "http://your-monitor-center:8080");
```

## 注意事项

1. **性能影响**：监控代理设计为轻量级，对应用性能影响极小
2. **网络异常**：如果无法连接到监控中心，不会影响应用正常运行
3. **资源清理**：应用关闭时建议调用 `NettyMonitor.shutdown()` 清理资源
4. **线程安全**：所有API都是线程安全的

## 故障排除

### 常见问题

1. **监控数据没有发送**
   - 检查监控中心是否启动
   - 检查网络连接
   - 查看应用日志

2. **初始化失败**
   - 确保在使用前调用了 `initialize()` 方法
   - 检查监控中心URL是否正确

3. **Handler添加失败**
   - 确保在 `initialize()` 之后调用 `getMonitorHandler()`
   - 检查 `isInitialized()` 返回值