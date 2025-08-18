package com.yueny.stars.netty.chats.server;

import com.yueny.stars.netty.chats.ConfigLoader;
import com.yueny.stars.netty.core.socket.server.AbstractChatServer;

import java.util.function.Consumer;

/**
 * 服务端启动类
 *
 * @author fengyang
 * @date 2025-08-18 16:57:49
 * @description
 */
public class ChatsServer extends AbstractChatServer {
    public static final ChatsServer INSTANCE = new ChatsServer();

    /**
     * 服务器端口号
     */
    private int port;

    private ChatsServer() {
        this(ConfigLoader.getServerPort());
    }

    public ChatsServer(int port) {
        super();

        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        INSTANCE.start(INSTANCE.port, new ChatsServerInitializer(), new Consumer<String>() {
            @Override
            public void accept(String text) {
                System.out.println("聊天服务器已启动，端口: " + INSTANCE.port);
            }
        });
    }
}
