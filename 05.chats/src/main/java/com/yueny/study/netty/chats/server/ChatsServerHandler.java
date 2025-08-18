package com.yueny.study.netty.chats.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * handler 是由 Netty 生成用来处理 I/O 事件的，是整个通信过程中的核心。
 *
 * @author fengyang
 * @date 2025-08-18 16:36:45
 * @description
 */
public class ChatsServerHandler extends SimpleChannelInboundHandler<String> {
    // 定义一个ChannelGroup来保存所有连接的Channel
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 维护每个连接对应的用户名
    private static final Map<Channel, String> channelToUsername = new ConcurrentHashMap<>();

    private String getDisplayName(Channel channel) {
        String name = channelToUsername.get(channel);
        return name != null && !name.isEmpty() ? name : String.valueOf(channel.remoteAddress());
    }

    /**
     * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当handler被添加到pipeline时，发送加入消息给所有Channel
        channels.writeAndFlush("【Server】 - " + getDisplayName(incoming) + " 加入\n");
        // 将当前Channel添加到ChannelGroup中
        channels.add(ctx.channel());
    }

    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 自动从 ChannelGroup 列表中移除了，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当handler从pipeline移除时，发送离开消息给所有Channel
        channels.writeAndFlush("【Server】 - " + getDisplayName(incoming) + " 离开\n");
        // 清理用户名映射
        channelToUsername.remove(incoming);
    }

    /**
     * 每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel incoming = ctx.channel();
        String trimmed = msg == null ? "" : msg.trim();

        // 处理改名命令：/name 新用户名
        if (trimmed.startsWith("/name ")) {
            String newName = trimmed.substring(6).trim();
            if (newName.isEmpty()) {
                incoming.writeAndFlush("用户名不能为空\n");
            } else {
                channelToUsername.put(incoming, newName);
                incoming.writeAndFlush("昵称已更新为：" + newName + "\n");
            }
            return;
        }

        // 遍历ChannelGroup中的所有Channel
        for (Channel channel : channels) {
            if (channel != incoming) {
                // 如果不是当前Channel，将消息发送给其他Channel，显示为来自incoming Channel
                channel.writeAndFlush("【" + getDisplayName(incoming) + "】" + msg + "\n");
            } else {
                // 如果是当前Channel，标记消息为"you"
                channel.writeAndFlush("【you】" + msg + "\n");
            }
        }
    }

    /**
     * 服务端监听到客户端活动
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当Channel活动时，打印在线信息
        System.out.println("SimpleChatClient：" + incoming.remoteAddress() + "在线");
    }

    /**
     * 服务端监听到客户端不活动
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当Channel非活动时，打印掉线信息
        System.out.println("SimpleChatClient：" + incoming.remoteAddress() + "掉线");
        // 清理用户名映射
        channelToUsername.remove(incoming);
    }

    /**
     * 当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        // 捕获异常，打印异常信息，并关闭Channel
        System.out.println("SimpleChatClient：" + incoming.remoteAddress() + "异常");
        cause.printStackTrace();
        // 清理用户名映射
        channelToUsername.remove(incoming);
        ctx.close();
    }
}
