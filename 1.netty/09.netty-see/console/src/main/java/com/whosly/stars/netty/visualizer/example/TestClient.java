package com.whosly.stars.netty.visualizer.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * 测试客户端，用于连接示例服务器
 * 
 * @author fengyang
 */
@Slf4j
public class TestClient {
    
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new TestClientHandler());
                        }
                    });
            
            ChannelFuture future = bootstrap.connect("localhost", 9999).sync();
            log.info("连接到服务器成功");
            
            Channel channel = future.channel();
            
            // 启动输入线程
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if ("quit".equals(input)) {
                        channel.close();
                        break;
                    }
                    channel.writeAndFlush(input + "\n");
                }
            }).start();
            
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    
    private static class TestClientHandler extends SimpleChannelInboundHandler<String> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("收到服务器消息: " + msg);
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("连接激活: {}", ctx.channel().remoteAddress());
            ctx.writeAndFlush("Hello from test client!\n");
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("客户端异常", cause);
            ctx.close();
        }
    }
}