package com.yueny.stars.netty.channel.serversocket.localhost;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * TCP客户端实现，与NioServerSocketChannel服务器通信
 *
 * @author fengyang
 * @date 2025-08-21 16:53:01
 * @description
 */
public class TcpClient {
    private final String host;
    private final int port;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        // 客户端事件循环组（无需区分boss和worker）
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // 客户端启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    // 指定客户端通道类型为NioSocketChannel
                    .channel(NioSocketChannel.class)
                    // 配置连接的服务器地址和端口
                    .remoteAddress(host, port)
                    // 禁用Nagle算法，减少延迟（小数据场景适用）
                    .option(ChannelOption.TCP_NODELAY, true)
                    // 保持长连接
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // 配置通道处理器，与服务器保持一致的编解码方式
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 使用与服务器相同的字符串解码器
                                    .addLast(new StringDecoder())
                                    // 使用与服务器相同的字符串编码器
                                    .addLast(new StringEncoder())
                                    // 添加客户端自定义处理器
                                    .addLast(new TcpClientHandler());
                        }
                    });

            // 连接服务器并同步等待连接成功
            ChannelFuture future = bootstrap.connect().sync();
            System.out.println("客户端已连接到服务器: " + host + ":" + port);

            // 发送测试消息到服务器
            future.channel().writeAndFlush("Hello from client!");

            // 等待连接关闭（阻塞直到连接被关闭）
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组，释放资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 连接本地8080端口的TCP服务器
        new TcpClient("localhost", 8080).start();
    }
}
