package com.yueny.study.netty.chat.server.enhanced;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 增强版服务器通道初始化器
 *
 * @author fengyang
 * @date 2025-08-18 09:49:51
 * @description
 */
public class EnhancedServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 添加字符串解码器和编码器
        // Socket/通道通信以字节流形式进行，字符串解码器和编码器帮助在字节和字符串之间转换
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        // 添加我们的自定义服务器处理器，包含聊天逻辑
        pipeline.addLast(new EnhancedChatServerHandler());
    }
}
