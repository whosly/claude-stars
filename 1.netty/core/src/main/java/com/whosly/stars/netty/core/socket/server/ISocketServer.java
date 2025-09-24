package com.whosly.stars.netty.core.socket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.function.Consumer;

/**
 * @author fengyang
 * @date 2025-08-18 14:10:53
 * @description
 */
public interface ISocketServer {
    ServerBootstrap start(int port, ChannelInitializer<SocketChannel> initializer, Consumer<String> consumer) throws Exception;

    void stop() throws Exception;
}
