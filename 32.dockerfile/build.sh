# 构建命令
docker build -t 10.50.91.10:1081/library/almalinux:9.6-arm ./
docker push 10.50.91.10:1081/library/almalinux:9.6-arm
docker save 10.50.91.10:1081/library/almalinux:9.6-arm -o alma9.6arm.tar
docker load -i alma9.6arm.tar