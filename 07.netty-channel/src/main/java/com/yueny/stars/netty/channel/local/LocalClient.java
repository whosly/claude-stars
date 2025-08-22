package com.yueny.stars.netty.channel.local;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * LocalChannel 客户端示例：连接本地服务器并进行字符串收发
 */
class LocalClient {
    private final String serverName;

    public LocalClient(String serverName) {
        this.serverName = serverName;
    }

    public void start() throws InterruptedException {
        DefaultEventLoopGroup group = new DefaultEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(LocalChannel.class)
                    .handler(new ChannelInitializer<LocalChannel>() {
                        @Override
                        protected void initChannel(LocalChannel ch) {
                            ch.pipeline()
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new LocalClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(new LocalAddress(serverName)).sync();
            System.out.println("客户端已连接到本地服务器：" + serverName);

            // 发送一条消息
            future.channel().writeAndFlush("Hello from LocalClient");

            // 等待通道关闭
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new LocalClient("my-local-server").start();
    }
}
