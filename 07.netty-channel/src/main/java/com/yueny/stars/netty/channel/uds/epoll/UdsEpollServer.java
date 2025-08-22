package com.yueny.stars.netty.channel.uds.epoll;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Only supported on Linux
 *
 * @author fengyang
 * @date 2025-08-21 11:17:58
 * @description
 */
public class UdsEpollServer {
    private final String socketPath;

    public UdsEpollServer(String socketPath) {
        this.socketPath = socketPath;
    }

    public void start() throws Exception {
        EventLoopGroup group = new EpollEventLoopGroup();
        try {
            Files.deleteIfExists(Paths.get(socketPath));

            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
             .channel(EpollServerDomainSocketChannel.class)
             .childHandler(new ChannelInitializer<EpollDomainSocketChannel>() {
                 @Override
                 protected void initChannel(EpollDomainSocketChannel ch) {
                     ch.pipeline()
                         .addLast(new StringDecoder())
                         .addLast(new StringEncoder())
                         .addLast(new UdsEpollServerHandler());
                 }
             });

            ChannelFuture f = b.bind(new DomainSocketAddress(socketPath)).sync();
            System.out.println("UDS(Epoll) 服务器已启动: " + socketPath);
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
            Files.deleteIfExists(Paths.get(socketPath));
        }
    }

    public static void main(String[] args) throws Exception {
        new UdsEpollServer("/tmp/netty-demo.sock").start();
    }
}
