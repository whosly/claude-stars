package com.yueny.stars.netty.chat.server.broadcast;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-18 14:28:00
 * @description
 */
public class BroadcastServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        /*
         * Socket/channel communication happens in byte streams. String decoder &
         * encoder helps conversion between bytes & String.
         */
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // This is our custom server handler which will have logic for chat.
        pipeline.addLast(new BroadcastChatServerHandler());
    }
}
