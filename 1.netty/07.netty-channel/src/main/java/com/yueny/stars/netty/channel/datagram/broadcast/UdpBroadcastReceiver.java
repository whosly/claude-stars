package com.yueny.stars.netty.channel.datagram.broadcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

/**
 * UDP 广播接收器，接收同一网络中的广播消息
 *
 * @author fengyang
 * @date 2025-08-21 19:17:32
 * @description
 */
public class UdpBroadcastReceiver {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
                            String message = packet.content().toString(CharsetUtil.UTF_8);
                            System.out.println("收到广播: " + message + " 来自: " + packet.sender());
                        }
                    });

            // 绑定到广播端口
            ChannelFuture future = bootstrap.bind(8888).sync();
            System.out.println("广播接收器已启动，等待广播消息...");

            future.channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }
}
