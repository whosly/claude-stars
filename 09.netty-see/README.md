# Netty-See 监控系统

一个专为 Netty 应用设计的实时监控系统，提供 Channel 连接监控、性能分析和可视化展示功能。

## 🚀 核心特性

- **智能注解系统**：通过 `@NettyMonitor` 注解即可启用监控
- **动态模板解析**：支持 `${username}`、`${server.port}` 等动态变量
- **无侵入集成**：通过 Pipeline Handler 自动拦截，无需修改业务代码
- **实时监控**：TCP 长连接实时数据传输和 Web 控制台展示
- **高可用设计**：监控故障不影响业务系统，提供降级处理
- **延迟初始化**：支持延迟初始化和自动重试机制

## 📦 模块架构

```
09.netty-see/
├── agent/          # 监控代理模块 - 嵌入到目标应用中
├── console/        # Web控制台模块 - 集成监控服务器和可视化界面
└── server/         # 独立监控服务器模块（可选）
```

## GUI

![index](./static/netty-see-index.png)
![channel](./static/netty-see-channel.png)
![channel-detail](./static/netty-see-channel-detail.png)
![loops](./static/netty-see-loops.png)
![performance](./static/netty-see-performance.png)
![errors](./static/netty-see-errors.png)
![buffer](./static/netty-see-buffer.png)


## 🎯 快速开始

### 1. 启动监控控制台
```bash
cd 09.netty-see/console
mvn spring-boot:run
```
访问 http://localhost:8081 查看监控面板

### 2. 在应用中集成监控

#### 方式1：注解方式（该方式尚未实现完全。开发中）
```
// 设置上下文变量
MonitorContextManager.setGlobalContext("username", "user1");
MonitorContextManager.setGlobalContext("server.port", "8080");

// 使用注解启用监控
@NettyMonitor(applicationName = "ChatsServer-${username}-${server.port}")
public class ChatsServer {
    public void start() {
        // 处理注解
        SmartMonitorAnnotationProcessor.getInstance().processClass(ChatsServer.class);
        
        // 在 Pipeline 中添加监控处理器
        if (NettyMonitorAgent.isInitialized()) {
            pipeline.addFirst("monitor", NettyMonitorAgent.getMonitorHandler());
        }
    }
}
```

#### 方式2：编程方式
```
// 初始化监控代理
NettyMonitorAgent.initialize("MyApp");

// 在 Pipeline 中添加监控处理器
pipeline.addFirst("monitor", NettyMonitorAgent.getMonitorHandler());
```

## 🔧 高级配置

### 注解配置选项
```java
@NettyMonitor(
    applicationName = "MyApp-${environment}-${version}",  // 支持动态变量
    host = "${monitor.host:localhost}",                   // 监控服务器地址
    port = 19999,                                         // 监控服务器端口
    lazyInit = true,                                      // 延迟初始化
    initTimeout = 10000,                                  // 初始化超时时间
    retryCount = 3,                                       // 重试次数
    retryInterval = 1000                                  // 重试间隔
)
public class MyNettyServer {
    // 服务器实现
}
```

### 上下文变量管理
```
// 设置全局上下文
MonitorContextManager.setGlobalContext("environment", "production");
MonitorContextManager.setGlobalContext("version", "1.0.0");

// 设置线程本地上下文
MonitorContextManager.setThreadContext("requestId", "req-123");

// 启用调试模式
MonitorContextManager.setDebugMode(true);
MonitorContextManager.dumpContext();
```

### 支持的变量语法
- `${variable}` - 从上下文或系统属性获取变量
- `${variable:default}` - 带默认值的变量
- `${methodName()}` - 调用对象方法获取值
- `${env.VARIABLE_NAME}` - 获取环境变量
- `${system.property}` - 获取系统属性

## 📊 监控数据

### Channel 信息
- Channel ID 和地址信息
- 连接状态和生命周期
- 数据传输统计（读写字节数）
- 用户关联和角色识别（CLIENT/SERVER）

### REST API
```bash
# 获取所有 Channel 信息
GET http://localhost:8081/api/netty/channels

# 获取统计信息
GET http://localhost:8081/api/netty/stats
```

## 🛠️ 开发和调试

### 获取处理器状态
```java
SmartMonitorAnnotationProcessor processor = SmartMonitorAnnotationProcessor.getInstance();

// 获取统计信息
int pendingCount = processor.getPendingInitializationCount();
int processedCount = processor.getProcessedClassCount();

// 获取错误统计
RetryErrorHandler.ErrorStatistics stats = processor.getErrorStatistics("MyClass");
```

### 模板验证
```java
TemplateResolver resolver = processor.getTemplateResolver();
TemplateResolver.ValidationResult result = resolver.validate("${username}-${server.port}");
System.out.println("模板有效: " + result.isValid());
```

## 🔍 故障排查

### 常见问题
1. **监控数据不显示**
   - 检查 NettyMonitorAgent 是否初始化成功
   - 确认 MonitorHandler 已添加到 Pipeline
   - 检查监控服务器是否运行（端口 19999）

2. **注解处理失败**
   - 检查上下文变量是否正确设置
   - 验证模板语法是否正确
   - 查看日志中的错误信息

3. **连接失败**
   - 检查网络连接和防火墙设置
   - 确认监控服务器地址和端口配置
   - 查看重试和错误处理日志

## 📚 文档

详细的设计文档和实现指南请参考：
- [设计文档](.kiro/specs/netty-see/design.md)
- [需求文档](.kiro/specs/netty-see/requirements.md)
- [架构文档](.kiro/specs/netty-see/architecture.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！