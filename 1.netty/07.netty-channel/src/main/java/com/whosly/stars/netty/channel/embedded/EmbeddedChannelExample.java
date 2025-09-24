package com.whosly.stars.netty.channel.embedded;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author fengyang
 * @date 2025-08-21 14:10:46
 * @description
 */
public class EmbeddedChannelExample {

    public static void main(String[] args) {
        testChannelPipeline();
    }

    public static void testChannelPipeline() {
        // 创建嵌入式通道，添加测试处理器
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new TestHandler()
        );

        // 写入测试数据
        String testData = "Hello, EmbeddedChannel!";
        ByteBuf buf = Unpooled.buffer().writeBytes(testData.getBytes());
        channel.writeInbound(buf);

        // 读取处理结果
        String received = channel.readInbound();
        // here, testData equals received

        // 测试出站数据
        String outboundData = "Test response";
        channel.writeOutbound(outboundData);

        ByteBuf response = channel.readOutbound();
        // here, outboundData equals response.toString()

        // 关闭通道
        channel.close();
    }

    static class TestHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("EmbeddedChannel 收到: " + msg);

            // 传递给下一个处理器
            ctx.fireChannelRead(msg);
        }
    }
}
