package com.whosly.stars.netty.channel.local;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-21 11:17:58
 * @description
 */
class LocalServer {
    private final String serverName;

    public LocalServer(String serverName) {
        this.serverName = serverName;
    }

    public void start() throws InterruptedException {
        // 本地事件循环组（使用非废弃的 DefaultEventLoopGroup）
        DefaultEventLoopGroup group = new DefaultEventLoopGroup();

        try {
            // 本地服务器启动辅助类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    // 指定使用 LocalServerChannel 作为本地服务器通道
                    .channel(LocalServerChannel.class)
                    // 绑定本地地址（名称）
                    .localAddress(new LocalAddress(serverName))
                    // 配置通道处理器
                    .childHandler(new ChannelInitializer<io.netty.channel.local.LocalChannel>() {
                        @Override
                        protected void initChannel(io.netty.channel.local.LocalChannel ch) {
                            ch.pipeline()
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new LocalServerHandler());
                        }
                    });

            // 绑定地址，同步等待成功
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("本地服务器已启动，名称：" + serverName);

            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new LocalServer("my-local-server").start();
    }
}
