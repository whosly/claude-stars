package com.yueny.stars.netty.msgpack.heartbeat.server;

import com.yueny.stars.netty.msgpack.heartbeat.code.MsgPackDecoder;
import com.yueny.stars.netty.msgpack.heartbeat.code.MsgPackEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
/**
 * @author fengyang
 * @date 2025-08-28 17:39:16
 * @description
 */
public class Server {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(4);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();

                            // 添加IdleStateHandler，设置空闲检测时间
                            // 参数：readerIdleTime, writerIdleTime, allIdleTime, timeUnit
                            p.addLast(new IdleStateHandler(5, 7, 10, TimeUnit.SECONDS));

//                            /**
//                             * 参数（1）maxFrameLength：表示的是包的最大长度，超出会做一些特殊处理。
//                             * 参数（2）lengthFieldOffset：指定长度域的偏移量，表示跳过指定长度个字节才是长度域；
//                             * 参数（3）lengthFieldLength：本数据帧的长度；
//                             * 参数（4）lengthAdjustment：该字段加长度字段等于数据帧的长度，包体长度调整的大小。
//                             * 参数（5）initialBytesToStrip：获取完一个完整的数据包之后，忽略前面的指定的位数个字节。
//                             */
//                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
                            p.addLast(new MsgPackDecoder());
                            p.addLast(new MsgPackEncoder());
                            /**
                             * 处理客户端的各种事件
                             */
                            p.addLast(new ServerHandler());
                        }
                    });

            ChannelFuture f = bootstrap.bind(8883).sync();
            System.out.println("服务器启动，监听端口: 8883");

            f.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
