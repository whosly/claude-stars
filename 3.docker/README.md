# Docker 模块

Docker 容器化实践项目，包含多个 Dockerfile 示例，涵盖基础镜像构建、多阶段构建、跨平台支持等容器化实践。

## 模块介绍

### 31.dockerfile-hello
基于 scratch 的基础镜像构建示例，展示如何从零开始构建一个简单的 Docker 镜像。

### 32.dockerfile-alpine-hello
基于 Alpine Linux 的镜像构建示例，展示如何使用轻量级 Linux 发行版构建镜像。

### 33.dockerfile-maven3-jdk-8-alpine
Maven + JDK 8 的 Alpine 镜像构建示例，展示 Java 应用的容器化部署。

### 34.dockerfile-arm
ARM 架构的 Dockerfile 示例，展示跨平台容器构建。

## 使用说明

进入相应的目录，使用以下命令构建镜像：

```bash
# 构建基础镜像
cd 31.dockerfile-hello
docker build -t dockerfile-hello:0.1 .

# 构建 Alpine 镜像
cd 32.dockerfile-alpine-hello
docker build -t dockerfile-alpine-hello:0.1 .
```

## 相关资源

- [Docker 官方文档](https://docs.docker.com/)
- [Dockerfile 最佳实践](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)