package com.yueny.stars.netty.channel.datagram.broadcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * UDP 广播发送器，向整个子网发送消息
 *
 * @author fengyang
 * @date 2025-08-21 19:16:16
 * @description
 */
public class UdpBroadcastSender {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    // 必须设置 SO_BROADCAST 选项才能发送广播
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                            // 广播发送器通常不需要处理接收的数据
                        }
                    });

            Channel channel = bootstrap.bind(0).sync().channel();

            // 广播地址 (通常是 255.255.255.255 或子网广播地址如 192.168.1.255)
            InetSocketAddress broadcastAddress = new InetSocketAddress("255.255.255.255", 8888);

            // 发送广播消息
            for (int i = 1; i <= 3; i++) {
                String message = "广播消息 #" + i;
                ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
                DatagramPacket packet = new DatagramPacket(buf, broadcastAddress);

                channel.writeAndFlush(packet);
                System.out.println("发送广播: " + message);

                // 等待2秒
                Thread.sleep(2000);
            }

            channel.close();
        } finally {
            group.shutdownGracefully();
        }
    }
}
