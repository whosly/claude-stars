package com.yueny.stars.netty.channel.uds.kqueue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-21 11:17:58
 * @description
 */
public class UdsKqueueClient {
    private final String socketPath;

    public UdsKqueueClient(String socketPath) {
        this.socketPath = socketPath;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new KQueueEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(KQueueDomainSocketChannel.class)
             .handler(new ChannelInitializer<KQueueDomainSocketChannel>() {
                 @Override
                 protected void initChannel(KQueueDomainSocketChannel ch) {
                     ch.pipeline()
                         .addLast(new StringDecoder())
                         .addLast(new StringEncoder())
                         .addLast(new UdsKqueueClientHandler());
                 }
             });

            ChannelFuture f = b.connect(new DomainSocketAddress(socketPath)).sync();
            System.out.println("UDS(KQueue) 客户端已连接: " + socketPath);
            f.channel().writeAndFlush("Hello over UDS(KQueue)");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new UdsKqueueClient("/tmp/netty-demo.sock").start();
    }
}
