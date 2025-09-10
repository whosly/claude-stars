如果需要基础镜像发挥更多的作用，我们还需要其他的工作。Docker中的容器运行在操作系统中，共享了操作系统的内核。

Linux内核仅提供了进程管理、内存管理、文件系统管理等一些基础的管理模块。除此之外，我们还需要一些Linux下的管理工具，包括`ls、cp、mv、tar`以及应用程序运行依赖的一些包。

目前比较流行的 rootfs 应该就是 alpine 了，因为他的体积特别小，最简单的环境只需要 5M。



**Dockerfile 中相关参数**

*   FROM 指的是依赖的基础镜像，如scratch表示的是空白的，从零开始的。依赖的镜像可以是本地的，也可以是远程库的
*   ADD 指的是添加本地文件到镜像中，如果遇到linux可解压格式文件，会自动解压，这就是为什么整个文件中没有对tar.gz进行显式解压
*   RUN 运行命令，如安装软件的相关命令
*   CMD 设置启动Container时默认执行的命令，这个可以在启动容器时覆盖



# FROM alpine

> \# 下载 RootFS（约 2.8MB）&#x20;
>
> wget <https://mirrors.tuna.tsinghua.edu.cn/alpine/v3.20/releases/aarch64/alpine-minirootfs-3.20.0-aarch64.tar.gz>&#x20;

项目下目录结构为：

```shell
Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
-a----         2025/9/10     18:22        3947706 alpine-minirootfs-3.20.0-aarch64.tar.gz
-a----         2025/9/10     18:23            217 Dockerfile
-a----         2025/9/10     18:18             43 hello.txt
```

## Dockerfile

```bash
FROM scratch

# 添加一个简单的文本文件
ADD hello.txt /

ADD alpine-minirootfs-3.20.0-aarch64.tar.gz /

# 配置容器启动命令（默认为 /bin/sh）
CMD ["/bin/sh"]

CMD [ "cat", "hello.txt" ]
```

## 构建镜像

```shell
$ cd 32.dockerfile-alpine

$ docker build -t dockerfile-alpine-hello:0.1 .
```

docker build 运行结果：

```markdown
[+] Building 0.5s (6/6) FINISHED                                                                                                                                                                               docker:desktop-linux
 => [internal] load build definition from Dockerfile                                                                                                                                                                           0.0s
 => => transferring dockerfile: 256B                                                                                                                                                                                           0.0s 
 => WARN: MultipleInstructionsDisallowed: Multiple CMD instructions should not be used in the same stage because only the last one will be used (line 9)                                                                       0.0s 
 => [internal] load .dockerignore                                                                                                                                                                                              0.0s 
 => [internal] load build context                                                                                                                                                                                              0.2s 
 => => transferring context: 3.95MB                                                                                                                                                                                            0.2s 
 => [1/2] ADD hello.txt /                                                                                                                                                                                                      0.0s 
 => [2/2] ADD alpine-minirootfs-3.20.0-aarch64.tar.gz /                                                                                                                                                                        0.1s 
 => exporting to image                                                                                                                                                                                                         0.1s 
 => => exporting layers                                                                                                                                                                                                        0.1s 
 => => writing image sha256:35371bad0a4bfd4ad886c8bde69963add9dc61ab98fc3862510bd6c73c3fa1f1                                                                                                                                   0.0s 
 => => naming to docker.io/library/dockerfile-alpine-hello:0.1                                                                                                                                                                 0.0s 

View build details: docker-desktop://dashboard/build/desktop-linux/desktop-linux/pyzj742e5xetvdtzcahtxnos4

 1 warning found (use docker --debug to expand):
 - MultipleInstructionsDisallowed: Multiple CMD instructions should not be used in the same stage because only the last one will be used (line 9)

What's next:
    View a summary of image vulnerabilities and recommendations → docker scout quickview
```

## **验证镜像**

```markdown
$ docker images dockerfile-alpine-hello
REPOSITORY                TAG       IMAGE ID       CREATED         SIZE
dockerfile-alpine-hello   0.1       35371bad0a4b   6 minutes ago   8.82MB


$ docker run --rm -it dockerfile-alpine-hello:0.1
Hello, Docker， this is dockerfile-alpine!

```

## 删除镜像

```markdown
$ docker rmi dockerfile-alpine-hello:0.1
Untagged: dockerfile-alpine-hello:0.1
Deleted: sha256:35371bad0a4bfd4ad886c8bde69963add9dc61ab98fc3862510bd6c73c3fa1f1
```

