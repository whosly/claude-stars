# Docker Hello Example

这是一个简单的Docker镜像示例，包含一个文本文件。

## 构建镜像

```bash
docker build -t dockerfile-hello:0.1 .
```

## 运行镜像

由于这是基于scratch的最小镜像，不能直接运行命令来查看内容。
但你可以通过以下方式查看镜像中的文件：

```bash
# 查看镜像中的文件系统
docker run --rm -it dockerfile-hello:0.1 sh

# 或者导出镜像内容查看
docker save dockerfile-hello:0.1 -o hello.tar
```

## 文件说明

- hello.txt: 包含简单的文本内容
- README.md: 本说明文件