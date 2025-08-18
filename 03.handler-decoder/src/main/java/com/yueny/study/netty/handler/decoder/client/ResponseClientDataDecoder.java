package com.yueny.study.netty.handler.decoder.client;

import com.yueny.study.netty.handler.decoder.ResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author fengyang
 * @date 2025-08-18 10:08:06
 * @description
 */
public class ResponseClientDataDecoder extends ReplayingDecoder<ResponseData> {

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) throws Exception {

        String respStringValue = in.readCharSequence(in.readInt(), RequestClientDataEncoder.CHARSET).toString();

        ResponseData data = ResponseData.builder().stringValue(respStringValue).build();

        out.add(data);
    }
}