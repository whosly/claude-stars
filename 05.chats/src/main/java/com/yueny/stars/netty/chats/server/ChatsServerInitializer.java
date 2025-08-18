package com.yueny.stars.netty.chats.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * ChannelInitializer是一个用于初始化新创建的Channel的类。在这里，它被用来设置几个自定义的处理器和编解码器
 * <p>
 *
 * <code>
 *     1. DelimiterBasedFrameDecoder: 这个解码器根据指定的分隔符（这里是换行符）来确定消息的边界。这对于处理基于文本的协议很有用。
 *     2. StringDecoder：将接收到的字节解码成字符串，以便后续处理。
 *     3. StringEncoder：将字符串编码为字节，以便发送到网络。
 *     4. ChatsServerHandler：这是自定义的服务器处理器，负责处理接收到的消息和事件。
 * </code>
 *
 * 最后，当一个新的SocketChannel被接受时，initChannel方法会被调用，它会设置好pipeline。
 *
 * @author fengyang
 * @date 2025-08-18 16:38:55
 * @description
 */
public class ChatsServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 获取Channel的pipeline
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 添加一个基于分隔符的帧解码器，这里使用换行符作为消息的分隔符，最大帧长度为8192字节
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));

        // 添加一个字符串解码器，将字节解码为字符串
        pipeline.addLast(new StringDecoder());

        // 添加一个字符串编码器，将字符串编码为字节
        pipeline.addLast(new StringEncoder());

        // 添加自定义的服务器处理器到pipeline
        pipeline.addLast(new ChatsServerHandler());

        // 打印客户端连接信息
        System.out.println("ChatsServerInitializer：" + socketChannel.remoteAddress() + "连接上了");
    }
}
