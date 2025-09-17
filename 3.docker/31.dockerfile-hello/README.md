# Docker Install

# 基础镜像

Docker 提供了两种方法来创建基础镜像，一种是通过引入tar包的形式，另外一种是通过一个空白的镜像来一步一步构建， 即既FROM scratch。

当使用Dockerfile定制镜像时，通常用`FROM`指令指定一个现有的镜像作为**父镜像**，如`FROM centos:7`。\
大部分的镜像都有一个父镜像，这使定制镜像变得简单。比如nginx容器使用 \`debian\` 作为父镜像，tomcat容器使用 \`jdk\` 作为父镜像。

除了选择现有镜像为父镜像外，还存在一个特殊的镜像，名为 scratch，它表示一个空白的镜。
FROM scratch表示从零开始构建一个镜像，那么这个Dockerfile构建出的镜像被称为基础镜像。
大部分的操作系统镜像都是基础镜像，如centos、oracle-linux、debian。

`scratch `是Docker保留镜像，镜像仓库中的任何镜像都不能使用这个名字，使用`FROM scratch`表明我们要构建镜像中的第一个文件层。

# 镜像分类

Docker Hub 上有非常多的高质量的官方镜像(docker团队制作的)。

有应用镜像，如 nginx、redis、mongo、mysql、httpd、tomcat 等；

有方便运行各种语言代码的编程语言镜像，如 node、oraclejdk，openjdk、python、ruby、golang 等。

还有更为基础的操作系统镜像，如 ubuntu、debian、centos、fedora、alpine 等。

# FROM scratch

## Dockerfile

构建一个简单的 scratch 镜像。下面是Dockerfile（Windows环境下）

```shell
FROM scratch

# 添加一个简单的文本文件
ADD hello.txt /

# 设置默认命令
CMD ["/hello.txt"]
```

## 构建

```markdown
# 进入 Dockerfile 根目录
$ cd 31.dockerfile-hello

# [images名称]:[版本]
# docker images | findstr dockerfile-hello
$ docker build -t dockerfile-hello:0.1 .

[+] Building 0.2s (6/6) FINISHED                                                                                                              docker:default
 => [internal] load build definition from Dockerfile                                                                                                    0.0s
 => => transferring dockerfile: 244B                                                                                                                    0.0s
 => [internal] load .dockerignore                                                                                                                       0.0s
 => => transferring context: 2B                                                                                                                         0.0s
 => [internal] load build context                                                                                                                       0.0s
 => => transferring context: 672B                                                                                                                       0.0s
 => [1/2] ADD hello.txt /                                                                                                                               0.0s
 => [2/2] ADD README.md /                                                                                                                               0.0s
 => exporting to image                                                                                                                                  0.0s
 => => exporting layers                                                                                                                                 0.0s
 => => writing image sha256:9ee810593c410fd21759d58ab70739970e5019adacda3c982012fd694ac0006d                                                            0.0s
 => => naming to docker.io/library/dockerfile-hello:0.1                                                                                                 0.0s
```

## 查看镜像

```markdown
# Windows环境和Linux环境通用
$ docker images dockerfile-hello
REPOSITORY         TAG       IMAGE ID       CREATED          SIZE
dockerfile-hello   0.1       9ee810593c41   23 seconds ago   596B

# Windows
$ docker images | findstr dockerfile-hello
```

这样就是一个基础的镜像。 但由于是个空镜像门内有任何指令依赖。因为不能被运行。
