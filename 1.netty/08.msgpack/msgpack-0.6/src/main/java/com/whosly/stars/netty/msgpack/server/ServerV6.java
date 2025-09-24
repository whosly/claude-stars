package com.whosly.stars.netty.msgpack.server;

import com.whosly.stars.netty.msgpack.code.MsgPack6Decoder;
import com.whosly.stars.netty.msgpack.code.MsgPack6Encoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author fengyang
 * @date 2025-08-28 10:52:27
 * @description
 */
public class ServerV6 {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();

        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /**
                             * 参数（1）maxFrameLength：表示的是包的最大长度，超出会做一些特殊处理。
                             * 参数（2）lengthFieldOffset：指定长度域的偏移量，表示跳过指定长度个字节才是长度域；
                             * 参数（3）lengthFieldLength：本数据帧的长度；
                             * 参数（4）lengthAdjustment：该字段加长度字段等于数据帧的长度，包体长度调整的大小。
                             * 参数（5）initialBytesToStrip：获取完一个完整的数据包之后，忽略前面的指定的位数个字节。
                             */
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
                            ch.pipeline().addLast(new MsgPack6Decoder());
                            /**
                             * 客户端使用 LengthFieldPrepender 给数据添加报文头Length字段，接受方使用 LengthFieldBasedFrameDecoder 进行解码
                             */
                            ch.pipeline().addLast(new LengthFieldPrepender(2));
                            ch.pipeline().addLast(new MsgPack6Encoder());
                            /**
                             * 处理客户端的各种事件
                             */
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(8883).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

}
