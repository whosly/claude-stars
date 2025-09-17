package com.yueny.stars.netty.channel.datagram.multi;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * UDP 多播接收器，加入多播组并接收消息
 *
 * @author fengyang
 * @date 2025-08-21 19:22:36
 * @description
 */
public class UdpMulticastReceiver {
    public static void main(String[] args) throws Exception {
        // IPv6 socket 无法加入 IPv4 的多播组。在 Windows 系统上，Java 默认可能会使用 IPv6 socket，而示例代码中多播地址是 IPv4 的（230.0.0.1），不配置如下，会不兼容。
        // 设置优先使用 IPv4 协议栈
        System.setProperty("java.net.preferIPv4Stack", "true");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.IP_MULTICAST_IF, getNetworkInterface())
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
                            String message = packet.content().toString(CharsetUtil.UTF_8);
                            System.out.println("收到多播: " + message + " 来自: " + packet.sender());
                        }
                    });

            // 绑定到多播端口
            Channel channel = bootstrap.bind(9999).sync().channel();

            // 获取 IPv4 网络接口
            NetworkInterface networkInterface = getIPv4NetworkInterface();
            if (networkInterface == null) {
                System.err.println("找不到可用的 IPv4 网络接口");
                return;
            }

            // 加入多播组
            InetAddress multicastAddress = InetAddress.getByName("230.0.0.1");
            InetSocketAddress groupAddress = new InetSocketAddress(multicastAddress, 9999);

            NioDatagramChannel nioChannel = (NioDatagramChannel) channel;
            nioChannel.joinGroup(groupAddress, networkInterface).sync();

            System.out.println("已加入多播组: 230.0.0.1:9999");
            System.out.println("使用网络接口: " + networkInterface.getDisplayName());
            System.out.println("多播接收器已启动，等待多播消息...");

            channel.closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 获取可用的网络接口
     */
    private static NetworkInterface getNetworkInterface() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                return networkInterface;
            }
        }
        return NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
    }

    /**
     * 获取 IPv4 网络接口
     */
    private static NetworkInterface getIPv4NetworkInterface() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            // 检查接口是否启用且不是回环接口
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                // 检查接口是否有 IPv4 地址
                boolean hasIPv4 = networkInterface.getInetAddresses().hasMoreElements();

                // 对于 Windows 系统，有时需要进一步检查
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 检查是否是 IPv4 地址
                    if (addr.getAddress().length == 4) { // IPv4 地址长度为 4 字节
                        return networkInterface;
                    }
                }
            }
        }

        // 如果找不到合适的接口，尝试返回默认接口
        try {
            return NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        } catch (Exception e) {
            return null;
        }
    }

}
