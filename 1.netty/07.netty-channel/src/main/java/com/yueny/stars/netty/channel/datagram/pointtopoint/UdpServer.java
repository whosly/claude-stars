package com.yueny.stars.netty.channel.datagram.pointtopoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * UDP 服务器和客户端示例，使用 Netty 的 NioDatagramChannel。
 *
 * @author fengyang
 * @date 2025-08-21 11:16:51
 * @description
 */
public class UdpServer {
    private final int port;

    public UdpServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        // 事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // UDP 服务器启动辅助类。 创建Bootstrap，注意这里使用的是Bootstrap而不是ServerBootstrap
            // UDP 是无连接的，不需要像 TCP 那样区分服务器和客户端引导类。
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    // 指定使用 NioDatagramChannel 作为 UDP 通道
                    .channel(NioDatagramChannel.class)
                    // 设置SO_BROADCAST选项。 UDP 不需要连接，设置为广播模式
                    .option(ChannelOption.SO_BROADCAST, true)
                    // UDP不需要监听连接，直接绑定端口即可
                    .localAddress(port)
                    // 配置通道处理器
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ch.pipeline()
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new UdpServerHandler());
                        }
                    });

            // 绑定端口，同步等待成功。 UDP不需要监听连接，直接绑定端口即可
//            ChannelFuture future = bootstrap.bind(port).sync();
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("UDP 服务器已启动，监听端口：" + port);

            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new UdpServer(8080).start();
    }
}

