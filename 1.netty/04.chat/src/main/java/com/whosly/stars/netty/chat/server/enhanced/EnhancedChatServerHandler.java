package com.whosly.stars.netty.chat.server.enhanced;

import io.netty.channel.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强版netty服务端处理器
 * 支持：@用户名/@ID 私聊、/name 改名、/users 查询、普通消息仅回发、可选广播
 */
public class EnhancedChatServerHandler extends SimpleChannelInboundHandler<String> {
    // username -> channel
    private static final Map<String, Channel> usernameToChannel = new ConcurrentHashMap<>();
    // id(shortText) -> channel
    private static final Map<String, Channel> idToChannel = new ConcurrentHashMap<>();
    // channel -> username
    private static final Map<Channel, String> channelToUsername = new ConcurrentHashMap<>();

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        String id = ch.id().asShortText();
        String tempClientName = "Client_" + id;

        // 建立映射
        channelToUsername.put(ch, tempClientName);
        usernameToChannel.put(tempClientName, ch);
        idToChannel.put(id, ch);

        // 发送欢迎与帮助
        String welcomeMsg = "欢迎连接到聊天服务器！\n"
                + "使用说明：\n"
                + "1. 普通消息：直接输入内容\n"
                + "2. 私聊消息：@用户名:消息内容 或 @" + id + ":消息内容\n"
                + "3. 广播消息：#广播:消息内容\n"
                + "4. 查看在线用户：/users\n"
                + "5. 修改用户名：/name 新用户名\n"
                + "6. 退出：/quit\n";
        ch.writeAndFlush(welcomeMsg);

        // 通知其他用户有新用户加入
        broadcastToOthers(ch, "系统: 新用户 " + tempClientName + " 加入了聊天室");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel senderChannel = ctx.channel();
        String senderName = channelToUsername.getOrDefault(senderChannel, "Unknown");
        String trimmed = msg == null ? "" : msg.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        // 命令处理
        if (trimmed.startsWith("/name ")) {
            handleRename(senderChannel, senderName, trimmed.substring(6).trim());
            return;
        }
        if ("/users".equalsIgnoreCase(trimmed)) {
            handleListUsers(senderChannel);
            return;
        }
        if ("/quit".equalsIgnoreCase(trimmed)) {
            handleQuit(senderChannel, senderName);
            return;
        }

        // 私聊：@目标:消息 目标可为用户名或短ID
        if (trimmed.startsWith("@")) {
            int colonIndex = trimmed.indexOf(":");
            if (colonIndex <= 1) {
                senderChannel.writeAndFlush("私聊格式错误，请使用: @目标:消息（目标为用户名或ID）\n");
                return;
            }
            String targetToken = trimmed.substring(1, colonIndex).trim();
            String content = trimmed.substring(colonIndex + 1).trim();
            if (content.isEmpty()) {
                senderChannel.writeAndFlush("消息内容不能为空\n");
                return;
            }
            handlePrivateMessage(senderChannel, senderName, targetToken, content);
            return;
        }

        // 广播
        if (trimmed.startsWith("#广播:")) {
            handleBroadcastMessage(senderChannel, senderName, trimmed);
            return;
        }

        // 普通消息：仅回发给发送者
        handleNormalMessage(senderChannel, senderName, msg);
    }

    private void handleNormalMessage(Channel senderChannel, String senderName, String msg) {
        String response = "服务器已收到您的消息: " + msg + "\n";
        senderChannel.writeAndFlush(response);
        System.out.println("[NORMAL] " + senderName + ": " + msg);
    }

    private void handlePrivateMessage(Channel senderChannel, String senderName, String targetToken, String content) {
        Channel target = usernameToChannel.get(targetToken);
        if (target == null) {
            target = idToChannel.get(targetToken);
        }
        if (target == null || !target.isActive()) {
            senderChannel.writeAndFlush("未找到目标或已离线: " + targetToken + "\n");
            return;
        }

        String senderId = senderChannel.id().asShortText();
        String targetName = channelToUsername.getOrDefault(target, "Unknown");
        String targetId = target.id().asShortText();

        target.writeAndFlush("私聊消息 [" + senderName + "|" + senderId + "]: " + content + "\n");
        senderChannel.writeAndFlush("已私聊发送至 [" + targetName + "|" + targetId + "]\n");
        System.out.println("[DM] " + senderName + " -> " + targetName + ": " + content);
    }

    private void handleBroadcastMessage(Channel senderChannel, String senderName, String msg) {
        String broadcastMsg = msg.substring(4);
        String fullMsg = "广播消息 [" + senderName + "]: " + broadcastMsg;
        for (Channel channel : usernameToChannel.values()) {
            if (channel.isActive()) {
                channel.writeAndFlush(fullMsg + "\n");
            }
        }
        senderChannel.writeAndFlush("广播消息已发送给所有在线用户\n");
        System.out.println("[BROADCAST] " + senderName + ": " + broadcastMsg);
    }

    private void handleListUsers(Channel senderChannel) {
        StringBuilder userList = new StringBuilder("在线用户列表:\n");
        for (Map.Entry<String, Channel> entry : usernameToChannel.entrySet()) {
            Channel ch = entry.getValue();
            String id = ch.id().asShortText();
            String name = entry.getKey();
            userList.append("- ").append(name).append(" (ID=").append(id).append(")\n");
        }
        senderChannel.writeAndFlush(userList.toString());
    }

    private void handleRename(Channel senderChannel, String oldName, String newNameRaw) {
        String newName = newNameRaw.trim();
        if (newName.isEmpty()) {
            senderChannel.writeAndFlush("用户名不能为空\n");
            return;
        }
        if (usernameToChannel.containsKey(newName)) {
            senderChannel.writeAndFlush("用户名已被占用: " + newName + "\n");
            return;
        }
        usernameToChannel.remove(oldName);
        usernameToChannel.put(newName, senderChannel);
        channelToUsername.put(senderChannel, newName);
        senderChannel.writeAndFlush("用户名已更新为: " + newName + "\n");
        System.out.println("[RENAME] " + oldName + " -> " + newName);
    }

    private void handleQuit(Channel senderChannel, String senderName) {
        broadcastToOthers(senderChannel, "系统: 用户 " + senderName + " 离开了聊天室");
        senderChannel.close();
    }

    private void broadcastToOthers(Channel excludeChannel, String message) {
        for (Channel channel : usernameToChannel.values()) {
            if (channel != excludeChannel && channel.isActive()) {
                channel.writeAndFlush(message + "\n");
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String clientName = channelToUsername.remove(channel);
        if (clientName != null) {
            usernameToChannel.remove(clientName);
        }
        idToChannel.values().remove(channel);
        System.out.println("Client disconnected: id=" + channel.id().asShortText() + ", name=" + clientName);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Closing connection for client - " + ctx);
        cause.printStackTrace();
        ctx.close();
    }
}
