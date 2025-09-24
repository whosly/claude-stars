package com.whosly.stars.netty.channel.serversocket.loopback;

import com.whosly.stars.netty.channel.serversocket.TcpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 本地回环 TCP 的跨进程通信
 */
public class TcpLoopbackServer {
    private final String host;
    private final int port;

    public TcpLoopbackServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.TCP_NODELAY, true)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline()
                         .addLast(new LineBasedFrameDecoder(8192))
                         .addLast(new StringDecoder())
                         .addLast(new StringEncoder())
                         .addLast(new TcpServerHandler());
                 }
             });

            ChannelFuture f = b.bind(host, port).sync();
            System.out.println("TCP 服务器已启动，监听 " + host + ":" + port);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new TcpLoopbackServer("127.0.0.1", 9000).start();
    }
}
