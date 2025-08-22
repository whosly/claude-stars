package com.yueny.stars.netty.channel.datagram.pointtopoint;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * UDP客户端处理器
 *
 * @author fengyang
 * @date 2025-08-21 18:58:18
 * @description
 */
class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        // 获取服务器地址
        InetSocketAddress sender = packet.sender();

        // 将接收到的数据转换为字符串
        ByteBuf content = packet.content();
        String message = content.toString(CharsetUtil.UTF_8);

        System.out.println("\n收到来自 " + sender + " 的响应: " + message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
