FROM maven:3-jdk-8-alpine

# 添加一个简单的文本文件
ADD hello.txt /

# Alpine Linux发行版使用的是apk包管理器，不是yum。
RUN apk add \
        net-tools \
        iproute2 \
        procps && \
    rm -rf /var/cache/apk/*

# 设置默认命令
CMD ["/bin/sh"]