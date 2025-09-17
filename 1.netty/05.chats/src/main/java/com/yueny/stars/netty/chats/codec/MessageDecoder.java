package com.yueny.stars.netty.chats.codec;

import com.yueny.stars.netty.chats.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Message对象解码器
 * @author fengyang
 */
public class MessageDecoder extends ByteToMessageDecoder {
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的字节来读取消息长度
        if (in.readableBytes() < 4) {
            return;
        }
        
        // 标记当前读取位置
        in.markReaderIndex();
        
        // 读取消息长度
        int messageLength = in.readInt();
        
        // 检查是否有足够的字节来读取完整的消息（时间戳8字节 + 消息内容）
        if (in.readableBytes() < 8 + messageLength) {
            // 重置读取位置
            in.resetReaderIndex();
            return;
        }
        
        // 读取时间戳
        long timestamp = in.readLong();
        
        // 读取消息内容
        byte[] messageBytes = new byte[messageLength];
        in.readBytes(messageBytes);
        String messageContent = new String(messageBytes, StandardCharsets.UTF_8);
        
        // 创建Message对象
        Message message = new Message(messageContent);
        message.setTimestamp(timestamp);
        
        // 只有有效消息才添加到输出列表
        if (message.isValid()) {
            out.add(message);
        } else {
            System.out.println("⚠️ MessageDecoder: 解码出无效消息，已忽略");
        }
    }
}