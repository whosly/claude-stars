package com.whosly.stars.netty.channel.serversocket.loopback;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 本地回环 TCP 的跨进程通信
 */
public class TcpLoopbackClient {
    private final String host;
    private final int port;

    public TcpLoopbackClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline()
                         .addLast(new LineBasedFrameDecoder(8192))
                         .addLast(new StringDecoder())
                         .addLast(new StringEncoder())
                         .addLast(new TcpLoopbackClientHandler());
                 }
             });

            ChannelFuture f = b.connect(host, port).sync();
            System.out.println("TCP 客户端已连接 " + host + ":" + port);
            f.channel().writeAndFlush("Hello over TCP" + System.lineSeparator());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new TcpLoopbackClient("127.0.0.1", 9000).start();
    }
}
