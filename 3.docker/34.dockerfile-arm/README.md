# FROM almalinux

## Dockerfile

```bash
FROM docker.1ms.run/library/almalinux:9.6
 
# 使用阿里云镜像源并安装必要工具
RUN sed -e 's|^mirrorlist=|#mirrorlist=|g' \
        -e 's|^# baseurl=https://repo.almalinux.org|baseurl=https://mirrors.aliyun.com|g' \
        -i.bak \
        /etc/yum.repos.d/almalinux*.repo && \
    yum -y update && \
    yum -y install \
        net-tools \
        iproute \
        procps-ng && \
    yum clean all && \
    rm -rf /var/cache/yum
 
# 设置默认命令
CMD ["/bin/bash"]
```

## 构建镜像

```shell
$ cd 34.dockerfile-arm

# 如果是远程构建，则需要加上ip信息
# docker build -t <ip>:<port>/<目录>/almalinux:9.6-arm ./
$ docker build -t dockerfile-arm:0.1 .
```

docker build 运行结果：

```markdown
[+] Building 192.8s (6/6) FINISHED                                                                                                                      docker:desktop-linux 
 => [internal] load build definition from Dockerfile                                                                                                                    0.0s 
 => => transferring dockerfile: 541B                                                                                                                                    0.0s 
 => [internal] load metadata for docker.1ms.run/library/almalinux:9.6                                                                                                   7.2s 
 => [internal] load .dockerignore                                                                                                                                       0.1s 
 => => transferring context: 2B                                                                                                                                         0.0s 
 => [1/2] FROM docker.1ms.run/library/almalinux:9.6@sha256:375aa0df1af54a6ad8f4dec9ec3e0df16feec385c4fb761ac5c5ccdd829d0170                                           173.7s 
 => => resolve docker.1ms.run/library/almalinux:9.6@sha256:375aa0df1af54a6ad8f4dec9ec3e0df16feec385c4fb761ac5c5ccdd829d0170                                             0.0s 
 => => sha256:83f3591fbd5ab72bc4785a7e60ce9484c33869faaccce715aaebd6218e1ab775 1.03kB / 1.03kB                                                                          0.0s 
 => => sha256:1e86a8eba5bf814057cadccc09b4304d4e029ed4adeea6cd2bf404fc907e4446 579B / 579B                                                                              0.0s 
 => => sha256:d2ed363a5c15bfdfcfe2ca43d50e09433fe29e65bd9c11a5e0f514412315f3a5 69.74MB / 69.74MB                                                                      169.6s 
 => => sha256:375aa0df1af54a6ad8f4dec9ec3e0df16feec385c4fb761ac5c5ccdd829d0170 4.70kB / 4.70kB                                                                          0.0s 
 => => extracting sha256:d2ed363a5c15bfdfcfe2ca43d50e09433fe29e65bd9c11a5e0f514412315f3a5                                                                               3.9s 
 => [2/2] RUN sed -e 's|^mirrorlist=|#mirrorlist=|g'         -e 's|^# baseurl=https://repo.almalinux.org|baseurl=https://mirrors.aliyun.com|g'         -i.bak          11.7s 
 => exporting to image                                                                                                                                                  0.1s 
 => => exporting layers                                                                                                                                                 0.1s 
 => => writing image sha256:d8ada354472cb868ab88acaca80263e5a31c6bb576addc5c4562a2b0f5def0e1                                                                            0.0s
 => => naming to docker.io/library/dockerfile-arm:0.1                                                                                                                   0.0s

View build details: docker-desktop://dashboard/build/desktop-linux/desktop-linux/hwk9ksh0e3gk49hssekh1sc3o

What's next:
    View a summary of image vulnerabilities and recommendations → docker scout quickview
```

## **验证镜像**

```markdown
$ docker images dockerfile-arm
REPOSITORY       TAG       IMAGE ID       CREATED         SIZE
dockerfile-arm   0.1       d8ada354472c   2 minutes ago   204MB


$ docker run --rm -it dockerfile-arm:0.1
[root@d72c7c6a3f94 /]# ls
afs  bin  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
[root@d72c7c6a3f94 /]# uname -r
5.15.167.4-microsoft-standard-WSL2
[root@d72c7c6a3f94 /]# uname -a
Linux d72c7c6a3f94 5.15.167.4-microsoft-standard-WSL2 #1 SMP Tue Nov 5 00:21:55 UTC 2024 x86_64 x86_64 x86_64 GNU/Linux
[root@d72c7c6a3f94 /]# cat /proc/version
Linux version 5.15.167.4-microsoft-standard-WSL2 (root@f9c826d3017f) (gcc (GCC) 11.2.0, GNU ld (GNU Binutils) 2.37) #1 SMP Tue Nov 5 00:21:55 UTC 2024
[root@d72c7c6a3f94 /]# exit
exit

$ docker ps -a
CONTAINER ID   IMAGE                        COMMAND                   CREATED          STATUS                      PORTS                                                             NAMES
d72c7c6a3f94   dockerfile-arm:0.1           "/bin/bash"               18 seconds ago   Up 17 seconds                                                                                 awesome_curran
```

## 删除镜像

```markdown
$ docker rmi dockerfile-arm:0.1
Untagged: dockerfile-arm:0.1
Deleted: sha256:d8ada354472cb868ab88acaca80263e5a31c6bb576addc5c4562a2b0f5def0e1
```

## 其他命令

```markdown
docker build -t <ip>:<port>/<目录>/almalinux:9.6-arm ./
docker push almalinux:9.6-arm
docker save almalinux:9.6-arm -o alma9.6arm.tar
docker load -i alma9.6arm.tar
```

