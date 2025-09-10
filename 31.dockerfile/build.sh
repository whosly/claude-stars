# 轻量级基础镜像制作
# arm 的基础镜像，当前镜像 200MB+
FROM docker.1ms.run/library/almalinux:9.6
#FROM maven:3-jdk-8-alpine

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
    rm -rf /var/cache/yum \

# 设置默认命令
CMD ["/bin/bash"]

WORKDIR /usr/src/app

COPY . /usr/src/app
RUN mvn package

ENV PORT 5000
EXPOSE $PORT
CMD [ "sh", "-c", "mvn -Dserver.port=${PORT} spring-boot:run" ]
