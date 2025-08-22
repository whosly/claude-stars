package com.yueny.stars.netty.chats.codec;

import com.yueny.stars.netty.chats.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * Message对象编码器
 * @author fengyang
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        if (msg != null && msg.getMessage() != null) {
            // 将消息内容转换为字节并写入ByteBuf
            byte[] messageBytes = msg.getMessage().getBytes(StandardCharsets.UTF_8);
            out.writeInt(messageBytes.length); // 先写入消息长度
            out.writeLong(msg.getTimestamp()); // 写入时间戳
            out.writeBytes(messageBytes); // 写入消息内容
        }
    }
}