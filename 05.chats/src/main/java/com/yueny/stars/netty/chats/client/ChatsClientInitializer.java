package com.yueny.stars.netty.chats.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-18 17:03:20
 * @description
 */
class ChatsClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 添加自定义的Message编解码器
        pipeline.addLast(new com.yueny.stars.netty.chats.codec.MessageDecoder());
        pipeline.addLast(new com.yueny.stars.netty.chats.codec.MessageEncoder());
        
        pipeline.addLast(new ChatsClientHandler());
    }
}