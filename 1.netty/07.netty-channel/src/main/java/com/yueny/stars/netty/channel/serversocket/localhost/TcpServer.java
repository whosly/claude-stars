package com.yueny.stars.netty.channel.serversocket.localhost;

import com.yueny.stars.netty.channel.serversocket.TcpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-21 11:10:08
 * @description
 */
public class TcpServer {
    private final int port;

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        // 主事件循环组，负责接收客户端连接
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 工作事件循环组，负责处理客户端数据
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 服务器端启动辅助类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    // 指定使用 NioServerSocketChannel 作为服务器通道
                    .channel(NioServerSocketChannel.class)
                    // 绑定端口
                    .localAddress(port)
                    // 设置服务器通道的选项
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置客户端通道的选项
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 配置客户端通道的处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new TcpServerHandler());
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("TCP 服务器已启动，监听端口：" + port);

            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new TcpServer(8080).start();
    }
}
