package com.yueny.study.netty.chat.server.enhanced;

import com.yueny.study.netty.chat.core.ConfigLoader;
import com.yueny.study.netty.core.socket.server.AbstractChatServer;

import java.util.function.Consumer;

/**
 * 增强版聊天服务器： 支持私聊(@ID)、广播和服务器回复功能
 *
 * @author fengyang
 * @date 2025-08-18 09:47:31
 * @description
 */
public class EnhancedChatServer extends AbstractChatServer {
    private static final int PORT = ConfigLoader.getServerPort();

    public static final EnhancedChatServer INSTANCE = new EnhancedChatServer();

    private EnhancedChatServer() {
        super();
    }

    public static void main(String[] args) throws Exception {
        INSTANCE.start(PORT, new EnhancedServerChannelInitializer(), new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("增强版聊天服务器已启动，端口: " + PORT);
                System.out.println("支持功能：");
                System.out.println("1. 普通消息：只向发送者回复");
                System.out.println("2. 私聊消息：@用户名:消息内容");
                System.out.println("3. 广播消息：#广播:消息内容");
                System.out.println("4. 查看在线用户：/users");
                System.out.println("5. 退出聊天：/quit");
            }
        });
    }
}
