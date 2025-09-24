package com.whosly.stars.netty.chat.server.broadcast;

import com.whosly.stars.netty.chat.ConfigLoader;
import com.whosly.stars.netty.core.socket.server.AbstractChatServer;

/**
 * 广播聊天
 *
 * @author fengyang
 * @date 2025-08-18 14:18:56
 * @description
 */
public class BroadcastChatServer extends AbstractChatServer {
    private static final int PORT = ConfigLoader.getServerPort();

    public static final BroadcastChatServer INSTANCE = new BroadcastChatServer();

    private BroadcastChatServer() {
        super();
    }

    public static void main(String[] args) throws Exception {
        INSTANCE.start(PORT, new BroadcastServerChannelInitializer(), null);
    }
}
