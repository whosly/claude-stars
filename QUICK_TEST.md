# 快速测试指南

## 修复内容

1. **修复了ChatsClient的输入流问题** - 不再在try-with-resources中关闭Scanner
2. **添加了跨平台支持** - Windows使用TCP连接，Unix使用LocalChannel
3. **添加了日志配置** - 解决SLF4J警告
4. **改进了用户交互** - 添加了退出命令和更好的提示

## 测试步骤

### 1. 启动监控中心
```bash
cd 09.netty-see/console
mvn spring-boot:run
```

应该看到：
- `TCP monitor server started on port: 19999` (Windows)
- `Local monitor server started on socket: /tmp/netty-monitor.sock` (Unix)

### 2. 启动聊天服务器
```bash
cd 05.chats
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.chats.server.ChatsServer"
```

应该看到：
- `监控代理已启用，将通过本地Socket发送监控数据`
- `Connected to monitor server via TCP at: localhost:19999` (Windows)
- `聊天服务器已启动，端口: 8081`

### 3. 启动聊天客户端
```bash
cd 05.chats
mvn exec:java -Dexec.mainClass="com.yueny.stars.netty.chats.client.ChatsClient"
```

输入用户名后应该看到：
- `Welcome [用户名]`
- `已连接到聊天服务器！`
- `>` 提示符

### 4. 测试聊天功能
- 输入消息并按回车发送
- 输入 `quit` 退出客户端

### 5. 查看监控界面
打开浏览器访问：http://localhost:8080

应该能看到：
- 连接统计信息
- Channel详细信息
- 实时数据更新

## 关键改进

### ChatsClient修复
- 移除了try-with-resources导致的Stream closed错误
- 添加了优雅的退出机制
- 改进了用户交互体验

### 跨平台监控
- Windows: 使用TCP连接 (localhost:19999)
- Unix/Linux/Mac: 使用LocalChannel (/tmp/netty-monitor.sock)
- 自动检测操作系统并选择合适的通信方式

### 监控功能
- 实时Channel状态监控
- 应用注册和管理
- 数据传输统计
- 异常监控

## 故障排除

### 如果ChatsClient仍然报错
1. 确保没有在调试模式下运行
2. 直接使用命令行运行，不要在IDE中调试
3. 检查端口8081是否被占用

### 如果监控连接失败
1. 确保监控中心先启动
2. 检查端口19999是否被占用
3. 查看控制台日志了解具体错误

### 如果Web界面无数据
1. 确保聊天服务器成功连接到监控中心
2. 启动聊天客户端建立连接
3. 检查浏览器控制台是否有WebSocket错误