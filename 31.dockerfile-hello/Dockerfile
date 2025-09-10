FROM scratch

# 添加一个简单的文本文件
ADD hello.txt /

# 提供一个说明文件，解释如何查看内容
ADD README.md /

# 设置容器启动时显示文件内容
CMD ["cat", "/hello.txt"]