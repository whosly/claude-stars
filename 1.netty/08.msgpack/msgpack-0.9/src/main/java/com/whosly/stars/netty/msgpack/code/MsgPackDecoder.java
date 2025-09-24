package com.whosly.stars.netty.msgpack.code;

import com.whosly.stars.netty.msgpack.domain.HeartbeatData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePackException;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.List;

/**
 * 解码器
 *
 * @author fengyang
 * @date 2025-08-28 14:11:30
 * @description
 */
public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext,
                          ByteBuf msg, List<Object> list) throws Exception {
        int length = msg.readableBytes();
        byte[] array = new byte[length];
        msg.getBytes(msg.readerIndex(), array, 0, length);

        // 反序列化
        try {
            HeartbeatData unpackedHeartbeatData = deserialize(array);
            System.out.println("Unpacked : " + unpackedHeartbeatData);

            list.add(unpackedHeartbeatData);
        } catch (IOException | MessagePackException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反序列化方法
     */
    private static HeartbeatData deserialize(byte[] data) throws IOException, MessagePackException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data)) {
            int type = unpacker.unpackInt();
            int seatId = unpacker.unpackInt();
            int speed = unpacker.unpackInt();
            String memo = unpacker.unpackString();

            return HeartbeatData.builder()
                    .type(type).seatId(seatId).speed(speed).memo(memo)
                    .build();
        }
    }
}
