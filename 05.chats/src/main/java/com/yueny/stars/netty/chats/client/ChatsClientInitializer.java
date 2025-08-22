package com.yueny.stars.netty.chats.client;

import com.yueny.stars.netty.monitor.agent.NettyMonitorAgent;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author fengyang
 * @date 2025-08-18 17:03:20
 * @description
 */
class ChatsClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 添加监控Handler（必须在最前面）
        if (NettyMonitorAgent.isInitialized()) {
            pipeline.addFirst("monitor", NettyMonitorAgent.getMonitorHandler());
        }

        // 添加自定义的Message编解码器
        pipeline.addLast(new com.yueny.stars.netty.chats.codec.MessageDecoder());
        pipeline.addLast(new com.yueny.stars.netty.chats.codec.MessageEncoder());
        
        pipeline.addLast(new ChatsClientHandler());
    }
}