package com.whosly.stars.netty.chat.client.enhanced;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 增强版客户端处理器
 * 提供更好的消息显示和用户体验
 *
 * @author fengyang
 * @date 2025-08-18 10:09:10
 * @description
 */
public class EnhancedChatClientHandler extends SimpleChannelInboundHandler<String> {
    
    /*
     * 打印从服务器接收到的聊天消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 格式化输出消息，使其更易读
        if (msg.startsWith("私聊消息")) {
            System.out.println("\n🔒 " + msg);
        } else if (msg.startsWith("广播消息")) {
            System.out.println("\n📢 " + msg);
        } else if (msg.startsWith("系统:")) {
            System.out.println("\n⚡ " + msg);
        } else if (msg.startsWith("在线用户列表")) {
            System.out.println("\n👥 " + msg);
        } else if (msg.startsWith("服务器已收到")) {
            System.out.println("\n✅ " + msg);
        } else if (msg.startsWith("欢迎连接到")) {
            System.out.println("\n🎉 " + msg);
        } else if (msg.startsWith("使用说明")) {
            System.out.println("\n📖 " + msg);
        } else {
            System.out.println("\n💬 " + msg);
        }
        
        // 显示输入提示
        System.out.print("> ");
    }
    
    /**
     * 连接建立时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("已连接到聊天服务器！");
        super.channelActive(ctx);
    }
    
    /**
     * 连接断开时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与服务器的连接已断开");
        super.channelInactive(ctx);
    }
    
    /**
     * 发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("客户端发生异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
