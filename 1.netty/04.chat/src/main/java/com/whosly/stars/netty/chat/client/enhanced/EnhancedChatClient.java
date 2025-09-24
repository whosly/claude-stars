package com.whosly.stars.netty.chat.client.enhanced;

import com.whosly.stars.netty.chat.ConfigLoader;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

/**
 * 增强版聊天客户端： 支持私聊(@ID)、广播和服务器, 支持更好的用户交互和消息显示
 *
 *
 * @author fengyang
 * @date 2025-08-18 10:07:17
 * @description
 */
public class EnhancedChatClient {
    private static final String HOST = ConfigLoader.getServerHost();
    private static final int PORT = ConfigLoader.getServerPort();
    static String clientName;

    public static void main(String[] args) throws InterruptedException {
        /*
         * 获取用户名称用于此聊天会话
         */
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("请输入您的用户名: ");
            if (scanner.hasNext()) {
                clientName = scanner.nextLine();
                System.out.println("欢迎 " + clientName + " 加入聊天室！");
            }

            /*
             * 配置客户端
             */

            // 由于这是客户端，不需要boss组。创建单个组
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap
                        // 设置EventLoopGroup来处理客户端的所有事件
                        .group(workerGroup)
                        // 使用NIO接受新连接
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch)
                                    throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                /*
                                 * Socket/通道通信以字节流形式进行，字符串解码器和编码器帮助在字节和字符串之间转换
                                 */
                                p.addLast(new StringDecoder());
                                p.addLast(new StringEncoder());

                                // 这是我们的自定义客户端处理器，包含聊天逻辑
                                p.addLast(new EnhancedChatClientHandler());
                            }
                        });

                // 启动客户端
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                Channel channel = future.channel();

                System.out.println("已连接到服务器 " + HOST + ":" + PORT);
                // 将启动时输入的用户名绑定到服务器（替换临时用户名）
                if (clientName != null && !clientName.trim().isEmpty()) {
                    channel.writeAndFlush("/name " + clientName.trim());
                }
                System.out.println("使用说明：");
                System.out.println("1. 普通消息：直接输入内容");
                System.out.println("2. 私聊消息：@用户名:消息内容");
                System.out.println("3. 广播消息：#广播:消息内容");
                System.out.println("4. 查看在线用户：/users");
                System.out.println("5. 退出聊天：/quit");
                System.out.println("开始聊天吧！\n");

                /*
                 * 迭代并获取用户的聊天消息输入，然后发送到服务器
                 */
                while (scanner.hasNext()) {
                    String input = scanner.nextLine();

                    if (input.equals("/quit")) {
                        System.out.println("正在退出聊天...");
                        channel.writeAndFlush(input);
                        break;
                    }

                    // 发送消息到服务器
                    channel.writeAndFlush(input);
                    channel.flush();
                }

                // 等待连接关闭
                future.channel().closeFuture().sync();
            } finally {
                // 关闭事件循环以终止所有线程
                workerGroup.shutdownGracefully();
            }
        }
    }
}