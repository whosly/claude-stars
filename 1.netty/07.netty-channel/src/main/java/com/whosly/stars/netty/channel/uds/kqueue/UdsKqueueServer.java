package com.whosly.stars.netty.channel.uds.kqueue;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Only supported on OSX/BSD
 *
 * @author fengyang
 * @date 2025-08-21 11:17:58
 * @description
 */
public class UdsKqueueServer {
    private final String socketPath;

    public UdsKqueueServer(String socketPath) {
        this.socketPath = socketPath;
    }

    public void start() throws Exception {
        EventLoopGroup group = new KQueueEventLoopGroup();
        try {
            Files.deleteIfExists(Paths.get(socketPath));

            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
             .channel(KQueueServerDomainSocketChannel.class)
             .childHandler(new ChannelInitializer<KQueueDomainSocketChannel>() {
                 @Override
                 protected void initChannel(KQueueDomainSocketChannel ch) {
                     ch.pipeline()
                         .addLast(new StringDecoder())
                         .addLast(new StringEncoder())
                         .addLast(new UdsKqueueServerHandler());
                 }
             });

            ChannelFuture f = b.bind(new DomainSocketAddress(socketPath)).sync();
            System.out.println("UDS(KQueue) 服务器已启动: " + socketPath);
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
            Files.deleteIfExists(Paths.get(socketPath));
        }
    }

    public static void main(String[] args) throws Exception {
        new UdsKqueueServer("/tmp/netty-demo.sock").start();
    }
}
