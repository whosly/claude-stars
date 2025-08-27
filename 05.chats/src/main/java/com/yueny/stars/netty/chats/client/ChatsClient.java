package com.yueny.stars.netty.chats.client;

import com.yueny.stars.netty.chats.ConfigLoader;
import com.yueny.stars.netty.chats.Message;
import com.yueny.stars.netty.core.socket.client.AbstractChatClient;
import com.yueny.stars.netty.monitor.agent.NettyMonitorAgent;
import com.yueny.stars.netty.monitor.agent.annotation.NettyMonitor;
import com.yueny.stars.netty.monitor.agent.context.MonitorContextManager;
import io.netty.channel.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * @author fengyang
 * @date 2025-08-18 17:03:50
 * @description
 */
//@NettyMonitor(applicationName = "ChatsServer-${username}-${server.port}", lazyInit = true, initTimeout = 10000,
//        retryCount = 3,  retryInterval = 1000)
public class ChatsClient extends AbstractChatClient {
    private static final String HOST = ConfigLoader.getServerHost();
    private static final int PORT = ConfigLoader.getServerPort();

    private ChatsClient(String clientName) {
        super(clientName);
    }

    public static void main(String[] args) throws Exception {
        MonitorContextManager.setGlobalContext("server.port",PORT);

        /*
         * Get name of the user for this chat session.
         */
        String clientName = "N/A";
        Scanner scanner = new Scanner(System.in);

        // 循环直到获得有效的用户名
        do {
            System.out.print("Please enter your name: ");
            clientName = scanner.nextLine();
            // 去除前后空白字符和换行符
            clientName = clientName.trim();
        } while (clientName.isEmpty());

        System.out.println("Setting name to: " + clientName);
        System.out.println("Welcome " + clientName);
        // 不要关闭scanner，因为我们后面还需要使用System.in

        String clientAlias = "ChatsClient-" + PORT + "-" + clientName;

        // NettyMonitor 方式， 不生效
//        MonitorContextManager.setGlobalContext("username", clientName);
//        // NettyMonitorAgent.initialize 方式， 手动启用客户端监控（包含端口信息）
        NettyMonitorAgent.initialize(clientAlias);
        System.out.println("✅ 客户端监控代理已启用，应用名称: " + clientAlias);

        ChatsClient chatClient = new ChatsClient(clientName);
        chatClient.start(HOST, PORT, new ChatsClientInitializer(), new Consumer<Channel>() {
            @Override
            public void accept(Channel channel) {
                // 设置用户名到channel属性中，供监控系统使用
                channel.attr(io.netty.util.AttributeKey.valueOf("username")).set(chatClient.getClientName());
                System.out.println("✅ 已设置channel用户名属性: " + chatClient.getClientName());

                // 创建BufferedReader，用于读取控制台输入
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                // 连接成功后，先向服务器注册用户名。 通过 channel 发送到服务器端
                channel.writeAndFlush(new Message("/name " + chatClient.getClientName()));

                // 循环读取控制台输入并发送到服务器
                while (true) {
                    try {
                        String input = in.readLine();
                        // 检查输入是否为空或只包含空白字符
                        if (input != null && !input.trim().isEmpty()) {
                            channel.writeAndFlush(new Message(input));
                        } else {
                            System.out.println("⚠️ 不能发送空消息，请输入有效内容");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
