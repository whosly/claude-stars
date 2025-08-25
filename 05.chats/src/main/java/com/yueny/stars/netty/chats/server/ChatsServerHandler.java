package com.yueny.stars.netty.chats.server;

import com.yueny.stars.netty.chats.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * handler 是由 Netty 生成用来处理 I/O 事件的，是整个通信过程中的核心。
 *
 * @author fengyang
 * @date 2025-08-18 16:36:45
 * @description
 */
class ChatsServerHandler extends SimpleChannelInboundHandler<Message> {
    // 定义一个ChannelGroup来保存所有连接的Channel，管理所有的 channel
    // GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 维护每个连接对应的用户名
    private static final Map<Channel, String> channelToUsername = new ConcurrentHashMap<>();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String getDisplayName(Channel channel) {
        String name = channelToUsername.get(channel);
        return name != null && !name.isEmpty() ? name : String.valueOf(channel.remoteAddress());
    }

    /**
     * handlerAdded 表示连接建立，一旦连接，第一个被执行
     *
     * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // 将当前 channel 加入到 channelGroup
        Channel incoming = ctx.channel();

        // 将该客户加入聊天的信息推送到给其他在线的客户端
        // 该方法会将 channelGroup 中所有的 channel 遍历，并发送消息，不需要自己遍历
        // 当handler被添加到pipeline时，发送加入消息给所有Channel
        channels.writeAndFlush(new Message("【Server】 - " + getDisplayName(incoming) + " 加入聊天 " + simpleDateFormat.format(new Date())));
        // 将当前Channel添加到ChannelGroup中
        channels.add(ctx.channel());
    }

    /**
     * 断开连接，将 xx客户离开信息推送给当前在线的客户
     *
     * 每当从服务端收到客户端断开时，客户端的 Channel 自动从 ChannelGroup 列表中移除了，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当handler从pipeline移除时，发送离开消息给所有Channel
        channels.writeAndFlush(new Message("【Server】 - " + getDisplayName(incoming) + " 离线了~~~"));
        // 清理用户名映射
        channelToUsername.remove(incoming);
    }

    /**
     * 每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        // 获取到当前 channel
        Channel incoming = ctx.channel();

        // 检查消息是否有效
        if (!msg.isValid()) {
            System.out.println("收到无效消息，忽略处理 - " + getDisplayName(incoming));
            return;
        }

        String trimmed = msg.getTrimmedMessage();

        // 处理改名命令：/name 新用户名
        if (trimmed.startsWith("/name ")) {
            String newName = trimmed.substring(6).trim();
            // 清理用户名：去除换行符和前后空白
            newName = newName.replaceAll("[\\r\\n]+", "");
            
            if (newName.isEmpty()) {
                incoming.writeAndFlush(new Message("用户名不能为空，请输入有效的用户名"));
            } else {
                channelToUsername.put(incoming, newName);
                // 设置用户名到channel属性中，供监控系统使用
                incoming.attr(io.netty.util.AttributeKey.valueOf("username")).set(newName);
                incoming.writeAndFlush(new Message("欢迎来到聊天室，" + newName + "！昵称已设置成功。"));
                System.out.println("用户 " + newName + " 设置昵称成功 (" + incoming.remoteAddress() + ")");
            }
            return;
        }

        // 遍历ChannelGroup中的所有Channel
        for (Channel channel : channels) {
            if (channel != incoming) {
                // 不是当前的 channel，直接转发消息，将消息发送给其他Channel，显示为来自incoming Channel
                channel.writeAndFlush(new Message("【" + getDisplayName(incoming) + "】" + msg.getMessage()));
            } else {
                // 如果是当前Channel，标记消息为"you"
                channel.writeAndFlush(new Message("【you】" + msg.getMessage()));
            }
        }
    }

    /**
     * 服务端监听到客户端活动, 表示 channel 处于活动状态，提示 xx上线
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当Channel活动时，打印在线信息
        System.out.println("SimpleChatClient：" + incoming.remoteAddress() + " 上线了~~~~");
    }

    /**
     * 服务端监听到客户端不活动, 表示 channel 处于非活动状态，提示 XX离线
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        // 当Channel非活动时，打印掉线信息
        System.out.println("SimpleChatClient：" + incoming.remoteAddress() + " 离线了~~~~");
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
