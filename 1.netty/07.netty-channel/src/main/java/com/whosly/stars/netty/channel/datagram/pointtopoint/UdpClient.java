package com.whosly.stars.netty.channel.datagram.pointtopoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * UDP 客户端示例
 *
 * @author fengyang
 * @date 2025-08-21 18:57:48
 * @description
 */
public class UdpClient {
    private final String host;
    private final int port;
    private Channel channel;

    public UdpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new UdpClientHandler());
                        }
                    });

            // 绑定到随机可用端口口（UDP客户端通常不需要固定端口）
            ChannelFuture future = bootstrap.bind(0).sync();
            this.channel = future.channel();

            System.out.println("UDP客户端已启动，本地端口: " + ((InetSocketAddress) channel.localAddress()).getPort());

            // 启动控制台输入
            startConsoleInput();

            // 等待通道关闭
            channel.closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 发送消息到服务器
     */
    public void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            // 准备数据
            ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
            InetSocketAddress recipient = new InetSocketAddress(this.host, this.port);

            // 创建数据包
            DatagramPacket packet = new DatagramPacket(buf, recipient);

            // 发送数据
            channel.writeAndFlush(packet);
            System.out.println("已发送消息: " + message);
        } else {
            System.out.println("通道未就绪，无法发送消息");
        }
    }

    /**
     * 启动控制台输入监听
     */
    private void startConsoleInput() {
        new Thread(() -> {
            try {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.print("\n请输入要发送的消息(输入'quit'退出): ");
                    String message = scanner.nextLine();

                    if ("quit".equalsIgnoreCase(message)) {
                        if (channel != null) {
                            channel.close();
                        }
                        break;
                    }

                    sendMessage(message);
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "console-input-thread").start();
    }

    public static void main(String[] args) throws InterruptedException {
        new UdpClient("localhost", 8080).start();
    }
}
