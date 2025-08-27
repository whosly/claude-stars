# Netty-See 监控系统架构文档

> **文档版本**: v2.0  
> **最后更新**: 2024-12-19  
> **架构状态**: 包含最新的统计分析功能

## 项目概述
Netty-See 是一个专为 Netty 应用设计的企业级监控系统，提供实时监控、统计分析、性能诊断和可视化展示功能。本文档描述了系统的最新架构设计和实现细节。

### 核心特性
- ✅ **实时监控**: Channel、EventLoop、Buffer 实时状态监控
- ✅ **统计分析**: 多维度数据聚合和趋势分析 (2024-12-19 新增)
- ✅ **智能注解**: 基于注解的监控配置系统
- ✅ **可视化界面**: 现代化 Web 界面，支持多种图表展示
- ✅ **RESTful API**: 完整的 API 接口支持

## 系统架构

### 整体架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Netty App 1   │    │   Netty App 2   │    │   Netty App N   │
│  ┌───────────┐  │    │  ┌───────────┐  │    │  ┌───────────┐  │
│  │   Agent   │  │    │  │   Agent   │  │    │  │   Agent   │  │
│  └───────────┘  │    │  └───────────┘  │    │  └───────────┘  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │ TCP:19999
                    ┌─────────────▼─────────────┐
                    │    Console Application    │
                    │  ┌─────────────────────┐  │
                    │  │  LocalMonitorServer │  │ :19999
                    │  └─────────────────────┘  │
                    │  ┌─────────────────────┐  │
                    │  │  NettyMonitorService│  │
                    │  └─────────────────────┘  │
                    │  ┌─────────────────────┐  │
                    │  │   Spring Boot Web   │  │ :8081
                    │  └─────────────────────┘  │
                    └───────────────────────────┘
```

### 模块结构
#### 1. Agent 模块 (netty-see-agent)
监控代理模块，嵌入到目标 Netty 应用中进行数据收集。

**核心组件：**
##### 1.1 核心监控组件
- **MonitorAgent**: 监控代理核心类，负责连接管理和数据传输
- **MonitorHandler**: Netty Pipeline 处理器，拦截 Channel 事件
- **NettyMonitorAgent**: 静态工厂类，提供简化的初始化接口

##### 1.2 智能注解处理系统
- **SmartMonitorAnnotationProcessor**: 智能注解处理器
  - 解析 @NettyMonitor 注解
  - 支持占位符替换（${username}、${server.port}等）
  - 提供延迟初始化和超时控制
  - 错误处理和降级机制
- **MonitorContextManager**: 监控上下文管理器
  - 全局上下文变量管理
  - 线程局部上下文支持
  - 占位符解析引擎

##### 1.3 模板解析系统
- **TemplateResolver**: 模板解析器核心
  - 支持 ${variable} 和 ${variable:default} 语法
  - 缓存机制提高性能
  - 模板验证和调试支持
- **VariableResolver**: 变量解析器接口
  - ContextVariableResolver (优先级: 10)
  - MethodCallResolver (优先级: 20)
  - EnvironmentResolver (优先级: 40)
  - SystemPropertyResolver (优先级: 50)

##### 1.4 错误处理与重试机制
- **RetryErrorHandler**: 重试和错误处理器
  - 错误分类（网络、资源、配置、权限）
  - 智能重试决策
  - 指数退避算法
  - 最大重试次数控制
- **NoOpMonitor**: 降级监控器
  - 在监控系统不可用时提供空操作实现
  - 确保业务系统稳定运行

##### 1.5 数据模型与工具
- **ChannelInfo**: Channel 信息数据模型
- **MonitorMessage**: 监控消息数据模型
- **JsonUtil**: JSON 序列化工具
- **Logger**: 轻量级日志工具

#### 2. Console 模块 (netty-see-console)
集成了监控服务器和Web控制台的Spring Boot应用。

**核心组件：**
##### 2.1 监控数据接收
- **LocalMonitorServer**: 监控数据接收服务器
  - 监听 TCP 端口 19999
  - 接收来自 Agent 的监控数据
  - JSON 数据解析和处理

##### 2.2 业务服务层
- **NettyMonitorService**: 监控数据业务服务
  - Channel 信息存储和管理
  - 基础数据统计和聚合
  - 内存存储实现
  - 与统计聚合服务集成

- **StatisticsAggregationService**: 统计聚合服务 🆕
  - 多维度数据聚合 (时间/应用/EventLoop)
  - 实时统计计算和缓存
  - 时间窗口数据管理 (分钟/小时/天)
  - 自动数据清理和归档

##### 2.3 统计数据模型 🆕
- **TimeWindowStats**: 时间窗口统计模型
  - 连接数、请求数、响应时间等核心指标
  - 支持分钟、小时、天级别聚合
  - 原子操作保证线程安全
- **ApplicationStats**: 应用维度统计模型
  - 按应用名称分组的统计数据
  - 地理位置分布、时间分布分析
  - 错误类型统计和EventLoop使用统计
- **EventLoopStats**: EventLoop维度统计模型
  - EventLoop负载和性能统计
  - Channel分布和任务队列监控
  - 负载均衡分析和重平衡建议

##### 2.4 Web API 层
- **NettyVisualizerController**: 基础监控 API 控制器
  - `/api/netty/channels` - 获取所有 Channel 信息
  - `/api/netty/stats` - 获取基础统计信息
  - 实时数据查询接口

- **StatisticsController**: 统计分析 API 控制器 🆕
  - `/api/statistics/realtime` - 实时统计数据
  - `/api/statistics/timerange` - 时间范围统计查询
  - `/api/statistics/applications` - 应用维度统计
  - `/api/statistics/eventloops` - EventLoop维度统计
  - `/api/statistics/overview` - 统计概览
  - `/api/statistics/performance` - 性能指标
  - `/api/statistics/errors` - 错误统计

- **WebController**: Web 页面控制器
  - 静态页面路由 (/, /channels, /eventloops, /performance, /buffers, /errors)
  - `/statistics` - 统计分析页面 🆕
  - 前端资源服务

##### 2.5 配置与任务
- **WebSocketConfig**: WebSocket 配置类
- **NettyDataWebSocketHandler**: WebSocket 数据推送处理器
- **DataPushTask**: 定时数据推送任务
- **统计数据清理任务**: 自动清理过期的统计数据 🆕

#### 3. Server 模块 (netty-see-server)
独立的监控服务器模块（可选，当前主要使用 Console 模块）。

## 核心特性

### 1. 智能注解系统
支持通过注解方式简化监控配置：
```java
@NettyMonitor(applicationName = "ChatsServer-${server.port}")
public class ChatsServer {
    // 服务器实现
}
```

**占位符支持：**
- `${username}` - 用户名变量
- `${server.port}` - 服务器端口
- `${timestamp}` - 时间戳
- 自定义变量支持

### 2. Channel 角色识别
系统能够智能识别 Channel 的角色：
- **CLIENT**: 客户端连接
- **SERVER**: 服务器端连接

通过地址和端口信息进行启发式判断，避免重复统计。

### 3. 错误处理与重试
- **网络连接错误**: 自动重试，指数退避
- **配置错误**: 快速失败，记录日志
- **权限错误**: 记录日志，不重试
- **资源不足**: 降级处理，使用 NoOpMonitor

### 4. 数据聚合与统计 🆕
- **多维度聚合**: 时间维度、应用维度、EventLoop维度
- **实时计算**: 秒级数据更新和统计
- **智能分析**: 趋势预测、异常检测、负载分析
- **可视化展示**: 丰富的图表和统计界面

### 5. 性能优化
- **延迟初始化**: 减少应用启动时间
- **异步处理**: 避免阻塞业务线程
- **缓存机制**: 提高数据处理效率
- **轻量级设计**: 最小化对业务性能的影响
- **原子操作**: 保证统计数据的线程安全 🆕
- **分层聚合**: 减少重复计算，提高性能 🆕

## 数据流架构 🆕

### 整体数据流
```
Agent数据收集 → 事件处理 → 统计聚合 → 数据存储 → API接口 → 前端展示
     ↓              ↓           ↓           ↓          ↓         ↓
Channel事件 → MonitorHandler → StatisticsService → 内存缓存 → REST API → Web界面
```

### 统计数据流
```
原始事件 → 实时统计 → 分钟聚合 → 小时聚合 → 天聚合
    ↓         ↓          ↓         ↓        ↓
Channel事件 → 实时指标 → 1分钟窗口 → 1小时窗口 → 1天窗口
    ↓         ↓          ↓         ↓        ↓
应用统计 → EventLoop统计 → 趋势分析 → 历史数据 → 长期存储
```

### 数据处理流程
1. **事件收集**: MonitorHandler 拦截 Netty 事件
2. **数据传输**: Agent 通过 TCP 发送到 Console
3. **事件处理**: LocalMonitorServer 解析和分发事件
4. **统计聚合**: StatisticsAggregationService 实时计算统计指标
5. **数据存储**: 内存缓存 + 定时清理机制
6. **API服务**: REST API 提供数据查询接口
7. **界面展示**: Web界面实时展示统计数据

## 数据模型

### ChannelInfo
```java
public class ChannelInfo {
    private String channelId;           // Channel ID
    private String applicationName;     // 应用名称
    private String localAddress;        // 本地地址
    private String remoteAddress;       // 远程地址
    private boolean isActive;           // 是否活跃
    private long createTime;            // 创建时间
    private long lastActiveTime;        // 最后活跃时间
    private long bytesRead;             // 读取字节数
    private long bytesWritten;          // 写入字节数
    private String username;            // 用户名
    private String channelRole;         // Channel角色 (CLIENT/SERVER)
}
```

### MonitorEvent
```json
{
  "type": "CHANNEL_ACTIVE|CHANNEL_INACTIVE|CHANNEL_READ|CHANNEL_WRITE",
  "applicationName": "string",
  "channelInfo": "ChannelInfo",
  "timestamp": "long"
}
```

## 部署架构

### 1. 嵌入式部署（推荐）
```
┌─────────────────────────────────┐
│        Target Application       │
│  ┌─────────────────────────┐    │
│  │    Business Logic       │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │    Netty Pipeline       │    │
│  │  ┌─────────────────┐    │    │
│  │  │ MonitorHandler  │    │    │
│  │  └─────────────────┘    │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │    Monitor Agent        │    │
│  └─────────────────────────┘    │
└─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│      Console Application        │
│  ┌─────────────────────────┐    │
│  │  LocalMonitorServer     │    │
│  └─────────────────────────┘    │
│  ┌─────────────────────────┐    │
│  │  Spring Boot Web        │    │
│  └─────────────────────────┘    │
└─────────────────────────────────┘
```

### 2. 使用步骤
#### 步骤1: 添加依赖
```xml
<dependency>
    <groupId>com.yueny.stars</groupId>
    <artifactId>netty-see-agent</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 步骤2: 启用监控
```java
// 方式1: 注解方式
@NettyMonitor(applicationName = "MyApp-${server.port}")
public class MyNettyServer {
    // 服务器实现
}

// 方式2: 编程方式
NettyMonitorAgent.initialize("MyApp");
```

#### 步骤3: 添加监控处理器
```java
pipeline.addFirst("monitor", NettyMonitorAgent.getMonitorHandler());
```

#### 步骤4: 启动监控控制台
```bash
java -jar netty-see-console.jar
```
访问 http://localhost:8081 查看监控面板。

## 配置说明

### Agent 配置
```java
// 监控服务器地址配置
MonitorAgent.initialize("MyApp", "localhost", 19999);

// 上下文变量设置
MonitorContextManager.setGlobalContext("username", "user1");
MonitorContextManager.setGlobalContext("server.port", "8080");
```

### Console 配置
```yaml
# application.yml
server:
  port: 8081

monitor:
  server:
    port: 19999
  cleanup:
    interval: 300000  # 5分钟清理一次过期数据
```

## 性能指标

### 性能要求
- **低延迟**: 监控逻辑对业务性能影响 < 5%
- **高可用**: 监控系统故障不影响业务正常运行
- **内存占用**: Agent 内存占用 < 10MB
- **网络开销**: 监控数据传输 < 1KB/连接/分钟

### 监控指标
- **连接数统计**: 实时活跃连接数
- **数据传输量**: 读写字节数统计
- **连接生命周期**: 连接创建、激活、关闭时间
- **错误统计**: 连接异常和错误分类统计

## 扩展性设计

### 1. 插件化架构
- 支持自定义监控处理器
- 支持自定义数据存储后端
- 支持自定义告警规则

### 2. 多实例支持
- 支持多个应用实例监控
- 支持集群模式部署
- 支持负载均衡

### 3. 数据持久化
- 内存存储（默认）
- 数据库存储（扩展）
- 时序数据库支持（扩展）

## 安全考虑

### 1. 数据传输安全
- 支持 TLS 加密传输
- 支持访问控制列表
- 支持数据脱敏

### 2. 访问控制
- Web 控制台访问认证
- API 接口权限控制
- 操作审计日志

## 故障处理

### 1. 监控系统故障
- 自动降级到 NoOpMonitor
- 业务系统正常运行
- 故障恢复后自动重连

### 2. 网络故障
- 自动重试机制
- 指数退避算法
- 最大重试次数限制

### 3. 资源不足
- 内存使用监控
- 自动数据清理
- 降级处理机制

## 未来规划

### 1. 功能增强
- 支持更多 Netty 组件监控
- 支持自定义指标收集
- 支持实时告警通知

### 2. 性能优化
- 批量数据传输
- 数据压缩算法
- 异步处理优化

### 3. 生态集成
- Spring Boot Starter
- Prometheus 集成
- Grafana 仪表板
- Kubernetes 支持