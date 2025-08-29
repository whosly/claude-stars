# msgpack 模块说明

本目录包含使用 Netty 集成 MessagePack 的三个示例子项目，演进展示了从 MessagePack 0.6 API 到 0.9 核心 API 的编码/解码实现，以及在此之上的心跳与断线重连示例。

## 子项目一览

- `common`: 公共数据模型
  - `HeartbeatData`: 心跳/业务报文的简单数据结构（`type`, `seatId`, `speed`, `memo`）。
  - `TypeData`: 类型常量定义（`PING`, `PONG`, `CUSTOME` 等）。

- `msgpack-0.6`: 使用 MessagePack 0.6 版本 API 的编解码示例
  - 主要类：`ServerV6`, `ClientV6`, `MsgPack6Encoder`, `MsgPack6Decoder`, 以及对应的 `ServerHandler`/`ClientHandler`。
  - 特点：
    - 使用 `org.msgpack.MessagePack` 的旧 API 进行对象序列化/反序列化。
    - Netty 中通过 `LengthFieldBasedFrameDecoder` + `LengthFieldPrepender(2)` 实现基于长度字段的半包/粘包处理（2 字节长度）。

- `msgpack-0.9`: 使用 MessagePack 0.9 核心 API 的编解码示例
  - 主要类：`ServerV9`, `ClientV9`, `MsgPackEncoder`, `MsgPackDecoder`，以及对应的 `ServerHandler`/`ClientHandler`。
  - 特点：
    - 使用 `org.msgpack.core`：`MessageBufferPacker`/`MessageUnpacker` 进行手动字段级序列化/反序列化。
    - 同样采用 `LengthFieldBasedFrameDecoder` + `LengthFieldPrepender(2)` 进行拆包/粘包处理。

- `msgpack-0.9-heartbeat`: 基于 0.9 实现扩展了心跳、空闲检测与断线重连
  - 主要类：`Server`, `Client`, `MsgPackEncoder`, `MsgPackDecoder`, `AbstractChannelInboundHandler`, `ServerHandler`, `ClientHandler`。
  - 特点：
    - 服务端引入 `IdleStateHandler(5, 7, 10, TimeUnit.SECONDS)` 做读/写/全空闲检测。
    - 客户端内置定时任务与重连逻辑（`ScheduledExecutorService`），并周期性发送 `HeartbeatData`。
    - 编解码与 `msgpack-0.9` 类似，字段级打包与解包。

## 数据模型与编解码

- 数据模型：`common` 中的 `HeartbeatData` 与 `TypeData`，三套示例共享。
  - `HeartbeatData` 字段：`type`、`seatId`、`speed`、`memo`。
  - `TypeData`：`PING`/`PONG`/`CUSTOME` 以及服务端/座位等常量。

- 编码/解码：
  - 0.6 版本：
    - `MsgPack6Encoder` 使用 `new MessagePack().write(msg)` 将对象序列化为 `byte[]`。
    - `MsgPack6Decoder` 使用 `new MessagePack().read(array, HeartbeatDataV6.class)` 反序列化为对象。
  - 0.9 版本及心跳示例：
    - `MsgPackEncoder` 使用 `MessageBufferPacker` 按顺序 `packInt/packString` 写入字段。
    - `MsgPackDecoder` 使用 `MessageUnpacker` 按相同顺序 `unpackInt/unpackString` 读出字段。

- 拆包/粘包处理：
  - `LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2)` 与 `LengthFieldPrepender(2)` 搭配。
  - 客户端发送前在 ByteBuf 前置 2 字节长度，服务端按长度切帧（反之亦然）。

## 运行与验证

前置条件：
- JDK 8+，Maven 3.6+。

构建：
```bash
mvn -q -DskipTests clean package
```

### 运行 0.6 示例
1. 启动服务端：运行 `msgpack-0.6` 中的 `com.yueny.stars.netty.msgpack.server.ServerV6`。
2. 启动客户端：运行 `msgpack-0.6` 中的 `com.yueny.stars.netty.msgpack.client.ClientV6`。
3. 默认端口 `8883`，使用 2 字节长度头；观察服务端/客户端日志。

### 运行 0.9 示例
1. 启动服务端：运行 `msgpack-0.9` 中的 `com.yueny.stars.netty.msgpack.server.ServerV9`。
2. 启动客户端：运行 `msgpack-0.9` 中的 `com.yueny.stars.netty.msgpack.client.ClientV9`。
3. 同样监听 `8883`，采用 0.9 的字段级打包/解包。

### 运行 0.9 心跳与重连示例
1. 启动服务端：运行 `msgpack-0.9-heartbeat` 中的 `com.yueny.stars.netty.msgpack.heartbeat.server.Server`。
   - 服务端管线包含 `IdleStateHandler`，会触发空闲事件（读 5s / 写 7s / 总 10s）。
2. 启动客户端：运行 `msgpack-0.9-heartbeat` 中的 `com.yueny.stars.netty.msgpack.heartbeat.client.Client`。
   - 客户端含定时任务与重连机制，周期性构造并发送 `HeartbeatData`。
3. 断开/恢复网络或停止/重启服务端，可观察客户端重连与心跳行为。

## 关键类速览

- 0.6 编解码：
  - `MsgPack6Encoder` / `MsgPack6Decoder`
  - `ServerV6` / `ClientV6`

- 0.9 编解码：
  - `MsgPackEncoder` / `MsgPackDecoder`
  - `ServerV9` / `ClientV9`

- 0.9 心跳与重连：
  - `heartbeat.server.Server` / `heartbeat.client.Client`
  - `heartbeat.code.MsgPackEncoder` / `heartbeat.code.MsgPackDecoder`
  - `heartbeat.handler.AbstractChannelInboundHandler`

## 注意事项

- 0.9 示例需严格保证编解码字段顺序一致（打包/解包一致），否则会解包失败。
- 示例默认使用 `localhost:8883`，如需变更请修改对应启动类中的主机与端口。
- `LengthFieldBasedFrameDecoder` 的 5 个构造参数需与 `LengthFieldPrepender` 保持一致配置；当前示例为 2 字节长度头。

本目录包含使用 Netty + MessagePack 实现的多版本编解码与示例工程，涵盖：
- common：共享的领域模型（心跳数据、类型常量）
- msgpack-0.6：基于旧版 org.msgpack（MessagePack v0.6 API 风格）的客户端/服务端与编解码器
- msgpack-0.9：基于新版 org.msgpack:msgpack-core（v0.9+ API）的客户端/服务端与编解码器
- msgpack-0.9-heartbeat：在 0.9 基础上加入心跳与重连示例（含 IdleStateHandler 与定时重连）

### 子模块概览

- common
  - 位置：`08.msgpack/common`
  - 主要类：
    - `com.yueny.stars.netty.msgpack.domain.HeartbeatData`：心跳与业务数据载体（type/seatId/speed/memo）
    - `com.yueny.stars.netty.msgpack.domain.TypeData`：类型常量（PING/PONG/CUSTOME 等）

- msgpack-0.6
  - 位置：`08.msgpack/msgpack-0.6`
  - 特色：使用 `org.msgpack.MessagePack` 旧 API，编码/解码直接操作对象（示例类 `HeartbeatDataV6`）。
  - 主要类：
    - 客户端：`client.ClientV6`
    - 服务端：`server.ServerV6`
    - 编码器：`code.MsgPack6Encoder`（MessageToByteEncoder）
    - 解码器：`code.MsgPack6Decoder`（MessageToMessageDecoder<ByteBuf>）
  - 管线（pipeline）要点：使用 `LengthFieldBasedFrameDecoder` 与 `LengthFieldPrepender(2)` 实现报文定长头分帧。

- msgpack-0.9
  - 位置：`08.msgpack/msgpack-0.9`
  - 特色：使用 `org.msgpack:msgpack-core` 新 API，手动打包字段，跨端更可控。
  - 主要类：
    - 客户端：`client.ClientV9`
    - 服务端：`server.ServerV9`
    - 编码器：`code.MsgPackEncoder`（使用 `MessageBufferPacker`，按字段顺序写入）
    - 解码器：`code.MsgPackDecoder`（使用 `MessageUnpacker`，按相同顺序读出）
  - 管线要点：同样使用 `LengthFieldBasedFrameDecoder` 与 `LengthFieldPrepender(2)` 进行半包/粘包处理。

- msgpack-0.9-heartbeat
  - 位置：`08.msgpack/msgpack-0.9-heartbeat`
  - 特色：在 0.9 的编码方式上，加入心跳探活与客户端定时重连示例。
  - 主要类：
    - 客户端：`heartbeat.client.Client`（定时任务尝试连接、`sendData()` 周期发送 `HeartbeatData`）
    - 服务端：`heartbeat.server.Server`（`IdleStateHandler(5,7,10,SECONDS)` 进行空闲检测）
    - 编/解码器：`heartbeat.code.MsgPackEncoder` / `heartbeat.code.MsgPackDecoder`
  - 管线要点：示例中可选使用分帧；展示 IdleStateHandler 的使用方式与心跳处理骨架。

### 数据模型

- `HeartbeatData`
  - 字段：`type:int`、`seatId:int`、`speed:int`、`memo:String`
  - Lombok：`@Data @Builder @AllArgsConstructor` 等
- `TypeData`
  - 常量：`PING`、`PONG`、`CUSTOME`、以及 `PING_SEAT`、`PONG_SEAT`、`SERVER_RESPONSE`、`SERVER_RESISTANT` 等

### 编解码要点

- 0.6 版本
  - 编码：`MessagePack#write(Object)` 将对象序列化为字节
  - 解码：`MessagePack#read(byte[], Class)` 直接还原为对象（示例中使用 `HeartbeatDataV6`）

- 0.9 版本及 heartbeat
  - 编码：`MessageBufferPacker` 按固定字段顺序写入（type → seatId → speed → memo）
  - 解码：`MessageUnpacker` 按相同顺序读取，构造 `HeartbeatData`
  - 注意：双方字段顺序必须严格一致；对于 `memo` 为空的情况，heartbeat 版本示例使用占位 `"-"`

### 分帧与半包处理

- 为避免粘包/半包，`msgpack-0.6` 与 `msgpack-0.9` 示例均使用：
  - 入站：`LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2)`
  - 出站：`LengthFieldPrepender(2)`
- `msgpack-0.9-heartbeat` 中示例展示了无需分帧时的管线骨架，并给出如何加入分帧的注释参考。

### 运行示例

先在根目录或子模块执行构建：
```bash
mvn -U -T 1C -q clean package -DskipTests
```

- 运行 0.6 版本：
  1) 启动服务端：运行 `msgpack-0.6` 的 `server.ServerV6#main`
  2) 启动客户端：运行 `msgpack-0.6` 的 `client.ClientV6#main`

- 运行 0.9 版本：
  1) 启动服务端：运行 `msgpack-0.9` 的 `server.ServerV9#main`
  2) 启动客户端：运行 `msgpack-0.9` 的 `client.ClientV9#main`

- 运行 0.9-heartbeat 版本：
  1) 启动服务端：运行 `msgpack-0.9-heartbeat` 的 `heartbeat.server.Server#main`
  2) 启动客户端：运行 `msgpack-0.9-heartbeat` 的 `heartbeat.client.Client#main`

### 端口与连接

- 示例默认端口：`8883`
- 客户端连接目标：`localhost:8883`

### 关键差异速览

- **API 差异**：0.6 使用旧 `MessagePack` 对象直接序列化/反序列化；0.9 使用 `msgpack-core` 的 `MessageBufferPacker/MessageUnpacker` 进行手动打包
- **可控性**：0.9 手动控制字段顺序与类型，更易于与跨语言/跨版本对接
- **心跳机制**：heartbeat 模块增加 `IdleStateHandler` 与客户端定时重连、周期发送数据示例

### 注意事项

- 双端编解码字段顺序与类型必须保持一致
- 若引入分帧，入站与出站的长度字段设置需匹配
- 生产环境建议对异常进行更完备处理，并加上日志与超时/重试策略

