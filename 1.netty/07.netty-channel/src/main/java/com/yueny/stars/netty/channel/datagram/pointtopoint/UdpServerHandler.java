package com.yueny.stars.netty.channel.datagram.pointtopoint;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * UDP 服务器处理器
 *
 * @author fengyang
 * @date 2025-08-21 16:50:07
 * @description
 */
class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        // 获取发送方的地址
        InetSocketAddress sender = packet.sender();

        // 将接收到的数据转换为字符串
        ByteBuf content = packet.content();
        String message = content.toString(CharsetUtil.UTF_8);

        System.out.println("收到来自 " + sender + " 的消息: " + message);

        // 准备响应数据
        String response = "(Confirm)服务器已收到您的消息: " + message;
        ByteBuf buf = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8);

        // 创建响应数据包，发送回客户端
        DatagramPacket responsePacket = new DatagramPacket(buf, sender);
        ctx.writeAndFlush(responsePacket);

        System.out.println("已向 " + sender + " 发送响应");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}