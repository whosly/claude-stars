package com.yueny.stars.netty.msgpack.heartbeat.code;

import com.yueny.stars.netty.msgpack.domain.HeartbeatData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;

/**
 * 编码器
 *
 * @author fengyang
 * @date 2025-08-28 14:11:52
 * @description
 */
public class MsgPackEncoder extends MessageToByteEncoder<HeartbeatData> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          HeartbeatData msg, ByteBuf byteBuf) throws Exception {
        // 序列化
        byte[] packedData = serialize(msg);

        byteBuf.writeBytes(packedData);
    }

    /**
     * 序列化方法
     */
    private static byte[] serialize(HeartbeatData data) {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            // 打包对象的属性
            packer.packInt(data.getType());
            packer.packInt(data.getSeatId());
            packer.packInt(data.getSpeed());
            packer.packString(data.getMemo() != null ? data.getMemo() : "-");
            packer.close();

            return packer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();

            // 返回空字节数组以表示失败
            return new byte[0];
        }
    }
}
