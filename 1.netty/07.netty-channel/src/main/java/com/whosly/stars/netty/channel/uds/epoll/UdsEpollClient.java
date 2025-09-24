package com.whosly.stars.netty.channel.uds.epoll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-21 11:17:58
 * @description
 */
public class UdsEpollClient {
    private final String socketPath;

    public UdsEpollClient(String socketPath) {
        this.socketPath = socketPath;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new EpollEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(EpollDomainSocketChannel.class)
             .handler(new ChannelInitializer<EpollDomainSocketChannel>() {
                 @Override
                 protected void initChannel(EpollDomainSocketChannel ch) {
                     ch.pipeline()
                         .addLast(new StringDecoder())
                         .addLast(new StringEncoder())
                         .addLast(new UdsEpollClientHandler());
                 }
             });

            ChannelFuture f = b.connect(new DomainSocketAddress(socketPath)).sync();
            System.out.println("UDS(Epoll) 客户端已连接: " + socketPath);
            f.channel().writeAndFlush("Hello over UDS(Epoll)");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new UdsEpollClient("/tmp/netty-demo.sock").start();
    }
}
