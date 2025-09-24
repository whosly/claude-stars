package com.whosly.stars.netty.channel.datagram.multi;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * UDP 多播发送器， 向特定多播组发送消息
 *
 * @author fengyang
 * @date 2025-08-21 19:21:46
 * @description
 */
public class UdpMulticastSender {
    public static void main(String[] args) throws Exception {
        // 设置优先使用 IPv4 协议栈
        System.setProperty("java.net.preferIPv4Stack", "true");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                            // 多播发送器通常不需要处理接收的数据
                        }
                    });

            Channel channel = bootstrap.bind(0).sync().channel();

            // 多播组地址 (范围: 224.0.0.0 到 239.255.255.255)
            InetAddress multicastAddress = InetAddress.getByName("230.0.0.1");
            InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddress, 9999);

            // 发送多播消息
            for (int i = 1; i <= 5; i++) {
                String message = "多播消息 #" + i;
                ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
                DatagramPacket packet = new DatagramPacket(buf, multicastGroup);

                channel.writeAndFlush(packet);
                System.out.println("发送多播: " + message);

                // 等待1.5秒
                Thread.sleep(1500);
            }

            channel.close();
        } finally {
            group.shutdownGracefully();
        }
    }
}
