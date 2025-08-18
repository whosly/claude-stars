package com.yueny.study.netty.handler.decoder.server;

import com.yueny.study.netty.handler.decoder.RequestData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author fengyang
 * @date 2025-08-18 09:58:28
 * @description
 */
public class RequestDecoder extends ReplayingDecoder<RequestData> {

    public final static Charset CHARSET = Charset.forName("UTF-8");

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) throws Exception {

        // 从 ByteBuf 中读取数据
        RequestData.RequestDataBuilder data = RequestData.builder();
        data.intValue(in.readInt());

        int strLen = in.readInt();
        data.stringValue(in.readCharSequence(strLen, CHARSET).toString());

        out.add(data.build());
    }
}