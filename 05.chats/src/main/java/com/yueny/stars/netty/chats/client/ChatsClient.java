package com.yueny.stars.netty.chats.client;

import com.yueny.stars.netty.chats.ConfigLoader;
import com.yueny.stars.netty.core.socket.client.AbstractChatClient;
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
public class ChatsClient extends AbstractChatClient {
    private static final String HOST = ConfigLoader.getServerHost();
    private static final int PORT = ConfigLoader.getServerPort();

    private ChatsClient(String clientName) {
        super(clientName);
    }

    public static void main(String[] args) throws Exception {
        /*
         * Get name of the user for this chat session.
         */
        String clientName = "N/A";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your name: ");
        if (scanner.hasNext()) {
            clientName = scanner.nextLine();
            System.out.println("Welcome " + clientName);
        }

        ChatsClient chatClient = new ChatsClient(clientName);
        chatClient.start(HOST, PORT, new ChatsClientInitializer(), new Consumer<Channel>() {
            @Override
            public void accept(Channel channel) {
                // 创建BufferedReader，用于读取控制台输入
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                // 连接成功后，先向服务器注册用户名。 通过 channel 发送到服务器端
                channel.writeAndFlush("/name " + chatClient.getClientName() + "\r\n");

                // 循环读取控制台输入并发送到服务器
                while (true) {
                    try {
                        channel.writeAndFlush(in.readLine() + "\r\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
