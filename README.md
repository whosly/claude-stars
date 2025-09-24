# CLAUDE STARS

## 项目概述

Claude Stars 是一个计算机软件行业理论学习与实践项目，旨在通过实际编码加深对 Netty、Docker、Calcite 等核心技术的理解。

### 核心技术栈
- **JDK**: 17+（推荐 azul-17.0.14）
- **构建工具**: Maven 3.6.3 或更高版本
- **网络框架**: Netty 4.1.94.Final
- **辅助库**: Lombok 1.18.24, Hutool 5.6.1, Guava 33.3.0-jre
- **数据库**: MySQL Connector 8.3.0
- **日志框架**: Logback 1.2.12 + SLF4J
- **测试框架**: JUnit 5.11.2 + Mockito 4.11.0

## 项目模块结构

### 1. Netty 模块 (1.netty)
基于 Netty 4.1.94.Final 实现的网络编程学习和实践项目，包含以下子模块：

#### 基础入门模块
- **01.server-and-client**: Echo 服务器和客户端基础实现
- **02.server-and-client-application**: 完整的 Echo 应用程序
- **03.handler-decoder**: 编解码器实践

#### 聊天应用模块
- **04.chat**: 基础聊天系统
- **05.chats**: 高级聊天室系统，支持昵称显示与系统提示功能

#### 核心技术模块
- **06.netty-buffer**: ByteBuf 缓冲区深度学习
- **07.netty-channel**: Channel 通道全面解析，支持多种 Channel 类型
- **08.msgpack**: 使用 MessagePack 进行高效编解码

#### 高级特性模块
- **09.netty-see**: Netty 实时监控系统，提供可视化监控面板
- **11.netty-capacity**: Netty 性能和容量测试

#### 工具模块
- **core**: 核心工具和通用组件
- **utils**: 实用工具集合

### 3. Docker 模块 (3.docker)
Docker 容器化实践项目，包含多个 Dockerfile 示例：

- **31.dockerfile-hello**: 基于 scratch 的基础镜像构建
- **32.dockerfile-alpine-hello**: 基于 Alpine Linux 的镜像构建
- **33.dockerfile-maven3-jdk-8-alpine**: Maven + JDK 8 的 Alpine 镜像
- **34.dockerfile-arm**: ARM 架构的 Dockerfile 示例

### 4. Calcite 模块 (4.calcite)
基于 Apache Calcite 构建的查询解析和优化项目：

#### 解析器生成
- **41.load-parser-jj**: 获取 Calcite 源码中的 Parser.jj 文件
- **42.parser-jj-generator**: 根据模板生成 parser-jj 代码文件
- **43.parser-generator**: 生成 Parser Java 代码
- **44.auto-generator**: 自动化生成 Parser Java 代码

#### 自定义语法
- **45.new-grammar**: 新增自定义语法示例（如 CREATE MATERIALIZED VIEW）
- **46.calcite-schema**: 多种数据源加载示例
- **47.calcite-rule**: 基于 Avatica 实现数据库查询示例

#### Avatica 集成
- **48.avacita**: 基于 Avatica 实现 JDBC 驱动连接和查询，支持 SQL 改写和脱敏处理


## 快速开始

### 构建项目

**构建所有模块：**
```bash
mvn clean initialize
mvn package
mvn clean install
```

**构建单个模块：**
```bash
# 首先构建基础工具模块
mvn install -pl utils

# 然后构建特定模块，例如：
mvn install -pl 05.chats
mvn install -pl 09.netty-see
```

### 运行示例

#### 1. 聊天系统体验：
```bash
# 启动聊天服务器
cd 1.netty/05.chats
mvn exec:java -Dexec.mainClass="com.whosly.stars.netty.chats.server.ChatsServer"

# 启动聊天客户端（新终端）
mvn exec:java -Dexec.mainClass="com.whosly.stars.netty.chats.client.ChatsClient"
```

#### 2. 监控系统体验：
```bash
# 启动监控控制台
cd 1.netty/09.netty-see/console
mvn spring-boot:run

# 访问 http://localhost:8081 查看监控面板
```

#### 3. Channel 示例体验：
```bash
# TCP Socket 通信示例
cd 1.netty/07.netty-channel
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.channel.socket.SocketTcpServer"
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.channel.socket.SocketTcpClient"

# UDP 通信示例
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.channel.datagram.pointtopoint.UdpServer"
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.channel.datagram.pointtopoint.UdpClient"
```

#### 4. Docker 镜像构建：
```bash
# 构建基础镜像
cd 3.docker/31.dockerfile-hello
docker build -t dockerfile-hello:0.1 .

# 构建 Alpine 镜像
cd 3.docker/32.dockerfile-alpine-hello
docker build -t dockerfile-alpine-hello:0.1 .
```

## 模块详细说明

### Netty 模块

Netty 模块采用事件驱动架构（EDA）和 Reactor 模式，实现了多种网络通信场景：

1. **基础通信**: Echo 服务器和客户端展示了最基本的 Netty 使用方式
2. **编解码器**: 自定义数据格式的处理和编解码实现
3. **聊天系统**: 支持多客户端实时通信的聊天室应用
4. **缓冲区管理**: Netty ByteBuf 的完整实现和优化
5. **通道类型**: 支持 TCP、UDP、Unix 域套接字等多种通道类型
6. **监控系统**: 实时监控 Netty 应用的连接状态和性能指标

### Docker 模块

Docker 模块提供了多个容器化实践示例：

1. **基础镜像构建**: 从 scratch 构建最小化镜像
2. **Alpine Linux**: 使用轻量级 Linux 发行版构建镜像
3. **多阶段构建**: Maven + JDK 环境的多阶段构建示例
4. **跨平台支持**: ARM 架构的 Dockerfile 示例

### Calcite 模块

Calcite 模块基于语法解析和 AST 生成的编译器模式，实现了：

1. **解析器生成**: 从模板生成 SQL 解析器代码
2. **自定义语法**: 扩展 SQL 语法，支持物化视图等新特性
3. **查询优化**: 基于规则的查询优化实现
4. **Avatica 集成**: JDBC 驱动实现和 SQL 脱敏处理

## 开发环境搭建

1. 安装 JDK 1.8+ 并配置环境变量
2. 安装 Maven 并配置 settings.xml
3. 克隆项目到本地目录
4. 使用 Maven 构建项目：`mvn clean install`

## 相关资源

- [Netty 官方文档](https://netty.io/wiki/)
- [Java NIO 教程](https://docs.oracle.com/javase/tutorial/essential/io/nio.html)
- [Apache Calcite 官方文档](https://calcite.apache.org/docs/)
- [Docker 官方文档](https://docs.docker.com/)

