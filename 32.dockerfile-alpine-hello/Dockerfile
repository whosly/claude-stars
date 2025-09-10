FROM scratch

# 添加一个简单的文本文件
ADD hello.txt /

ADD alpine-minirootfs-3.20.0-aarch64.tar.gz /

# 配置容器启动命令（默认为 /bin/sh）
CMD ["/bin/sh"]

CMD [ "cat", "hello.txt" ]