package com.whosly.stars.netty.handler.decoder.client.code;

import com.whosly.stars.netty.handler.decoder.domain.ResponseData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author fengyang
 * @date 2025-08-18 10:08:06
 * @description
 */
public class ClientResponseDataDecoder extends ReplayingDecoder<ResponseData> {

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) throws Exception {

        String respStringValue = in.readCharSequence(in.readInt(), ClientRequestDataEncoder.CHARSET).toString();

        ResponseData data = ResponseData.builder().stringValue(respStringValue).build();

        out.add(data);
    }
}