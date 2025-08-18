package com.yueny.study.netty.handler.decoder.server;

import com.yueny.study.netty.handler.decoder.ResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author fengyang
 * @date 2025-08-18 09:59:36
 * @description
 */
public class ResponseDataEncoder extends MessageToByteEncoder<ResponseData> {

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          ResponseData msg, ByteBuf out) throws Exception {

        // server -> client， 将返回的数据编解码
        // 需要先写入字符串长度，再写入字符串内容，与客户端的解码器格式匹配
        String stringValue = msg.getStringValue();
        out.writeInt(stringValue.length());
        out.writeCharSequence(stringValue, RequestDecoder.CHARSET);
    }
}