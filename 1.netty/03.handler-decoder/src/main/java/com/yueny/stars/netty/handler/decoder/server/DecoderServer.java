package com.yueny.stars.netty.handler.decoder.server;

import com.yueny.stars.netty.handler.decoder.server.code.ServerRequestDecode;
import com.yueny.stars.netty.handler.decoder.server.code.ServerResponseDataEncode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author fengyang
 * @date 2025-08-18 09:47:31
 * @description
 */
public class DecoderServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(PORT))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                    new ServerRequestDecode(),
                                    new ServerResponseDataEncode(),
                                    new DecoderServerHandler());
                        }
                    })
                    //设置队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 两小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("DecoderServer started and listening for connections on " + future.channel().localAddress());

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully().sync();
        }
    }
}
