# Netty 聊天系统增强版

## 概述

这是一个 Netty 聊天系统，服务端支持向固定用户发送消息、广播给所有客户端发送消息。

## enhanced 功能
* 服务端：com.whosly.stars.netty.chat.server.enhanced 
  + EnhancedChatServer：调用 start(PORT, new EnhancedServerChannelInitializer(), consumer)；consumer 打印启动指引 
  + EnhancedServerChannelInitializer：挂载 StringDecoder、StringEncoder、EnhancedChatServerHandler 
  + EnhancedChatServerHandler：支持 
    - @用户名:消息 或 @ID:消息 私聊（ID 为 channel.id().asShortText()） 
    - /name 新名 改名（去重） 
    - /users 查看在线用户（用户名与ID） 
    - 普通消息仅回发发送者；#广播: 可选广播

* 客户端：com.whosly.stars.netty.chat.client.enhanced 
  + EnhancedChatClient：基于 Netty Bootstrap 启动，添加字符串编解码器与 EnhancedChatClientHandler，使用 try-with-resources 管理 Scanner 
  + EnhancedChatClientHandler：打印消息、连接/断开提示与异常处理

## 使用方法
* 启动服务端：com.whosly.stars.netty.chat.server.enhanced.EnhancedChatServer
* 启动客户端：com.whosly.stars.netty.chat.client.enhanced.EnhancedChatClient
  + 客户端在启动时，会要求用户输入自定义用户名。该用户名自定义之后， 客户端会连接建立后自动发送一次“/name 用户名”，服务端的 EnhancedChatServerHandler 会用该用户名替换临时名，从而完成用户名与该 channel ID 的绑定。。
* 命令与格式：
  + 私聊：@用户名:消息 或 @ID:消息 
  + 改名：/name 新名 
  + 在线用户：/users, 输出格式为 `yourName (ID=xxxx) `
  + 广播：#广播:内容 
  + 退出：/quit

### 1. 消息路由机制
- **普通消息**：只向发送者回复，不会广播给其他客户端
- **私聊消息**：支持指定用户名的私聊功能
- **广播消息**：可选择性地广播消息给所有用户
- **系统消息**：用户上线、下线等系统通知

### 2. 用户体验提升
- 清晰的消息类型标识（使用emoji图标）
- 详细的连接状态提示
- 友好的错误处理和用户指导

### 3. 线程安全
- 使用 `ConcurrentHashMap` 管理客户端连接
- 支持多客户端并发连接

## 配置

配置文件：`src/main/resources/application.properties`

```properties
server.port=8080
server.host=localhost
```

## 架构说明

### 服务器端
- `EnhancedChatServerHandler`: 核心消息处理逻辑
- `EnhancedChatServer`: 服务器启动类
- `EnhancedServerChannelInitializer`: 通道初始化器

### 客户端
- `EnhancedChatClientHandler`: 客户端消息处理
- `EnhancedChatClient`: 客户端启动类

### 关键特性
1. **消息隔离**：普通消息不会影响其他客户端
2. **选择性广播**：只有明确标记的广播消息才会发送给所有用户
3. **连接管理**：自动管理客户端连接和断开
4. **错误处理**：友好的错误提示和异常处理


## 例子

### 1. 普通消息
直接输入文本内容，服务器只向发送者回复确认。

**示例：**
```
> 你好，世界！
✅ 服务器已收到您的消息: 你好，世界！
```

### 2. 私聊消息
使用 `@用户名:消息内容` 或者  `@用户ID:消息内容` 格式发送私聊消息。

**示例：**
```
> @Client_123: 你好！
🔒 私聊消息 [Client_456]: 你好！
✅ 私聊消息已发送给 Client_123
```

### 3. 广播消息
使用 `#广播:消息内容` 格式发送广播消息。

**示例：**
```
> #广播:大家好！
📢 广播消息 [Client_456]: 大家好！
✅ 广播消息已发送给所有在线用户
```

### 4. 系统命令

#### 查看在线用户
```
> /users
👥 在线用户列表:
- Client_123
- Client_456
- Client_789
```

#### 退出聊天
```
> /quit
正在退出聊天...
与服务器的连接已断开
```

## 效果展示

> EnhancedChatServer

```
已连接到地址为 ''127.0.0.1:62571'，传输: '套接字'' 的目标虚拟机
Chat Server started. Ready to accept chat clients.

增强版聊天服务器已启动，端口: 8080
支持功能：
1. 普通消息：只向发送者回复
2. 私聊消息：@用户名:消息内容
3. 广播消息：#广播:消息内容
4. 查看在线用户：/users
5. 退出聊天：/quit

[RENAME] Client_070c91c2 -> poy
[RENAME] Client_8549e568 -> alias
[NORMAL] poy: \/users
[DM] poy -> alias: 你在哪儿
[DM] alias -> poy: beijing
[DM] poy -> alias: o
[DM] alias -> poy: 你呢

```

> EnhancedChatClient A 用户

```
已连接到地址为 ''127.0.0.1:62658'，传输: '套接字'' 的目标虚拟机
请输入您的用户名: 
alias
欢迎 alias 加入聊天室！
已连接到服务器 localhost:8080
已连接到聊天服务器！

🎉 欢迎连接到聊天服务器！
使用说明：
1. 普通消息：直接输入内容
2. 私聊消息：@用户名:消息内容 或 @8549e568:消息内容
3. 广播消息：#广播:消息内容
4. 查看在线用户：/users
5. 修改用户名：/name 新用户名
6. 退出：/quit

> 
💬 用户名已更新为: alias

> 
🔒 私聊消息 [poy|070c91c2]: 你在哪儿

> @070c91c2: beijing

💬 已私聊发送至 [poy|070c91c2]

> 
🔒 私聊消息 [poy|070c91c2]: o

> @poy: 你呢

💬 已私聊发送至 [poy|070c91c2]

> 
```

> EnhancedChatClient B 用户

```
已连接到地址为 ''127.0.0.1:62666'，传输: '套接字'' 的目标虚拟机
请输入您的用户名: 
poy
欢迎 poy 加入聊天室！
\已连接到服务器 localhost:8080
已连接到聊天服务器！

🎉 欢迎连接到聊天服务器！
使用说明：
1. 普通消息：直接输入内容
2. 私聊消息：@用户名:消息内容 或 @070c91c2:消息内容
3. 广播消息：#广播:消息内容
4. 查看在线用户：/users
5. 修改用户名：/name 新用户名
6. 退出：/quit

> 
💬 用户名已更新为: poy

> 
⚡ 系统: 新用户 Client_8549e568 加入了聊天室

> /users

✅ 服务器已收到您的消息: \/users

> /users

👥 在线用户列表:
- alias (ID=8549e568)
- poy (ID=070c91c2)

> @alias: 你在哪儿

💬 已私聊发送至 [alias|8549e568]

> 
🔒 私聊消息 [alias|8549e568]: beijing

> @8549e568: o

💬 已私聊发送至 [alias|8549e568]

> 
🔒 私聊消息 [alias|8549e568]: 你呢

> 
```