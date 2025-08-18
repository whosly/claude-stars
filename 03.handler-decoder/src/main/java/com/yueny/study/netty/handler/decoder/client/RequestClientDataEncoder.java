package com.yueny.study.netty.handler.decoder.client;

import com.yueny.study.netty.handler.decoder.RequestData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

/**
 * @author fengyang
 * @date 2025-08-18 10:07:40
 * @description
 */
public class RequestClientDataEncoder extends MessageToByteEncoder<RequestData> {

    public final static Charset CHARSET = Charset.forName("UTF-8");

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          RequestData msg, ByteBuf out) throws Exception {
        // 将发送的对象编解码发送
        out.writeInt(msg.getIntValue());
        out.writeInt(msg.getStringValue().length());
        out.writeCharSequence(msg.getStringValue(), CHARSET);
    }
}