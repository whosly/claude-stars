package com.yueny.stars.netty.channel.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author fengyang
 * @date 2025-08-21 11:11:44
 * @description
 */
public class SocketTcpClient {
    private final String host;
    private final int port;
    private SocketChannel socketChannel;

    public SocketTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        // 客户端事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // 客户端启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    // 指定使用 NioSocketChannel 作为客户端通道
                    .channel(NioSocketChannel.class)
                    // 设置通道选项
                    .option(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法，提高实时性
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 连接超时时间
                    .option(ChannelOption.SO_KEEPALIVE, true) // 开启TCP心跳机制
                    // 配置服务器地址和端口
                    .remoteAddress(host, port)
                    // 配置通道处理器
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 添加空闲状态检测处理器，5秒没有读操作触发
                                    .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                    // 添加字符串解码器
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    // 添加字符串编码器
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    // 添加心跳处理器
                                    .addLast(new HeartbeatHandler())
                                    // 添加业务处理器
                                    .addLast(new TcpClientHandler());
                        }
                    });

            // 连接服务器
            ChannelFuture future = bootstrap.connect().sync();
            System.out.println("已连接到服务器：" + host + ":" + port);

            // 保存SocketChannel引用，用于后续发送消息
            this.socketChannel = (SocketChannel) future.channel();

            // 启动控制台输入监听
            startConsoleInput();

            // 等待连接关闭
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组
            group.shutdownGracefully().sync();
        }
    }

    /**
     * 启动控制台输入监听，允许用户输入消息发送到服务器
     */
    private void startConsoleInput() {
        new Thread(() -> {
            try {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.print("请输入要发送的消息(输入'quit'退出): ");
                    String message = scanner.nextLine();

                    if ("quit".equalsIgnoreCase(message)) {
                        if (socketChannel != null) {
                            socketChannel.close();
                        }
                        break;
                    }

                    if (socketChannel != null && socketChannel.isActive()) {
                        // 发送消息到服务器
                        socketChannel.writeAndFlush(message);
                        System.out.println("已发送消息: " + message);
                    } else {
                        System.out.println("连接已断开，无法发送消息");
                        break;
                    }
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "console-input-thread").start();
    }

    public static void main(String[] args) throws InterruptedException {
        new SocketTcpClient("localhost", 8080).start();
    }
}