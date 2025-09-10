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