package com.yueny.stars.netty.msgpack.code;

import com.yueny.stars.netty.msgpack.domain.Student6Info;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 编码器
 *
 * @author fengyang
 * @date 2025-08-28 10:50:44
 * @description
 */
public class MsgPackEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          Object msg, ByteBuf byteBuf) throws Exception {
        // 新建一个MessagePack对象，将对象o转化为byte保存在ByteBuf中。
        MessagePack messagePack = new MessagePack();

        byte[] raw = messagePack.write(msg);
        byteBuf.writeBytes(raw);
    }
}