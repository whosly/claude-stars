package com.yueny.stars.netty.channel.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * Netty TCP 服务器示例
 *
 * @author fengyang
 * @date 2025-08-21 17:08:04
 * @description
 */
public class SocketTcpServer {
    private final int port;

    public SocketTcpServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        // 创建两个EventLoopGroup
        // bossGroup负责接受客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // workerGroup负责处理已接受的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建服务器启动引导类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    // 指定使用NioServerSocketChannel作为服务器通道实现
                    .channel(NioServerSocketChannel.class)
                    // 设置服务器选项
                    .option(ChannelOption.SO_BACKLOG, 128) // 连接队列大小
                    .option(ChannelOption.SO_REUSEADDR, true) // 地址重用
                    // 设置子通道选项
                    .childOption(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 开启TCP心跳
                    // 配置子通道处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 添加空闲状态检测处理器，10秒没有读写操作触发
                                    .addLast(new IdleStateHandler(10, 10, 10, TimeUnit.SECONDS))
                                    // 添加字符串解码器
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    // 添加字符串编码器
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    // 添加心跳处理器
                                    .addLast(new ServerHeartbeatHandler())
                                    // 添加业务处理器
                                    .addLast(new TcpServerHandler());
                        }
                    });

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("服务器已启动，监听端口: " + port);

            // 等待服务器通道关闭
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭EventLoopGroup
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new SocketTcpServer(8080).start();
    }
}
