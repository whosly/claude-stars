package com.yueny.stars.netty.msgpack.code;

import com.yueny.stars.netty.msgpack.domain.Student6Info;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * 解码器
 *
 * @author fengyang
 * @date 2025-08-28 10:51:34
 * @description
 */
public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext,
                          ByteBuf msg, List<Object> list) throws Exception {
        int length = msg.readableBytes();
        byte[] array = new byte[length];

        msg.getBytes(msg.readerIndex(), array, 0, length);

        // 通过MessagePack再将缓冲区的byte转化为对象
        MessagePack messagePack = new MessagePack();
        list.add(messagePack.read(array, Student6Info.class));
    }
}