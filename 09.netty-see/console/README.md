# Netty Console - 监控面板

基于 Spring Boot 的 Netty 监控可视化面板，提供实时的 Web 界面来查看和分析 Netty 应用程序的监控数据。

## 特性

- **实时监控**：通过 WebSocket 实时推送监控数据
- **可视化界面**：HTML5 + JavaScript 实现的现代化界面
- **RESTful API**：提供完整的 REST API 接口
- **多应用支持**：可以同时监控多个 Netty 应用程序

## 快速开始

### 1. 启动应用

```bash
cd console
mvn spring-boot:run
```

### 2. 访问界面

打开浏览器访问：http://localhost:8080

### 3. 查看监控数据

- **首页**：`/` - 监控概览
- **Channel详情**：`/channels` - 详细的Channel信息

## API 接口

### 监控数据接收

- `POST /api/monitor/data` - 接收监控代理发送的数据

### 查询接口

- `GET /api/channels` - 获取所有Channel信息
- `GET /api/channels/{channelId}` - 获取指定Channel信息
- `GET /api/eventloops` - 获取EventLoop信息
- `GET /api/buffers` - 获取Buffer信息

### WebSocket

- `ws://localhost:8080/netty-data` - 实时数据推送

## 配置

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: netty-console

logging:
  level:
    com.yueny.stars.netty.visualizer: DEBUG
```

### 自定义端口

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

## 界面功能

### 首页 (/)

- 连接总数统计
- 活跃连接数
- 实时连接状态图表

### Channel详情页 (/channels)

- Channel列表
- 连接详细信息
- 实时状态更新

## 开发

### 项目结构

```
console/
├── src/main/java/
│   └── com/yueny/stars/netty/visualizer/
│       ├── NettyVisualizerApplication.java    # 主启动类
│       ├── config/                            # 配置类
│       │   ├── WebSocketConfig.java          # WebSocket配置
│       │   └── NettyDataWebSocketHandler.java # WebSocket处理器
│       ├── controller/                        # REST控制器
│       │   ├── NettyVisualizerController.java # API控制器
│       │   └── WebController.java            # 页面控制器
│       ├── service/                          # 业务服务
│       │   └── NettyMonitorService.java      # 监控服务
│       ├── model/                            # 数据模型
│       │   ├── ChannelInfo.java             # Channel信息
│       │   ├── EventLoopInfo.java           # EventLoop信息
│       │   └── BufferInfo.java              # Buffer信息
│       ├── integration/                      # 集成工具
│       │   └── NettyMonitorIntegration.java  # 监控集成
│       ├── task/                            # 定时任务
│       │   └── DataPushTask.java            # 数据推送任务
│       └── example/                         # 示例代码
│           ├── ExampleNettyServer.java      # 示例服务器
│           └── TestClient.java              # 测试客户端
└── src/main/resources/
    ├── application.yml                       # 应用配置
    └── templates/                           # 页面模板
        ├── index.html                       # 首页
        └── channels.html                    # Channel详情页
```

### 添加新功能

1. **新增API接口**：在 `controller` 包下添加新的控制器
2. **新增数据模型**：在 `model` 包下添加新的数据模型
3. **新增业务逻辑**：在 `service` 包下添加新的服务类
4. **新增页面**：在 `templates` 目录下添加新的HTML模板

### 自定义监控指标

```java
@Service
public class CustomMonitorService {
    
    @Autowired
    private NettyMonitorService monitorService;
    
    public void addCustomMetric(String name, Object value) {
        // 添加自定义监控指标
        monitorService.addCustomMetric(name, value);
    }
}
```

## 部署

### Docker 部署

```dockerfile
FROM openjdk:8-jre-alpine
COPY target/netty-console-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 生产环境配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: prod

logging:
  level:
    root: INFO
    com.yueny.stars.netty.visualizer: INFO
  file:
    name: logs/netty-console.log
```

## 故障排除

### 常见问题

1. **WebSocket连接失败**
   - 检查防火墙设置
   - 确认端口没有被占用

2. **监控数据不显示**
   - 检查监控代理是否正确配置
   - 查看应用日志

3. **页面加载缓慢**
   - 检查网络连接
   - 考虑增加服务器资源