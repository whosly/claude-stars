package com.yueny.stars.netty.core.socket.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.function.Consumer;

/**
 * @author fengyang
 * @date 2025-08-18 14:35:02
 * @description
 */
public interface IClient {
    <R> boolean start(String serverHost, int serverPort, ChannelInitializer<SocketChannel> channelInitializer, Consumer<Channel> consumer) throws Exception;

    void stop() throws Exception;

}
