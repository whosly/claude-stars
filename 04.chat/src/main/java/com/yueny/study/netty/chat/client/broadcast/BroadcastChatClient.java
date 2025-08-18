package com.yueny.study.netty.chat.client.broadcast;

import com.yueny.study.netty.chat.core.ConfigLoader;
import com.yueny.study.netty.core.socket.client.AbstractChatClient;
import io.netty.channel.Channel;

import java.util.Scanner;
import java.util.function.Consumer;

/**
 * 广播聊天
 *
 * @author fengyang
 * @date 2025-08-18 15:02:34
 * @description
 */
public class BroadcastChatClient extends AbstractChatClient {
    private static final String HOST = ConfigLoader.getServerHost();
    private static final int PORT = ConfigLoader.getServerPort();

    private BroadcastChatClient(String clientName) {
        super(clientName);
    }

    public static void main(String[] args) throws Exception {
        /*
         * Get name of the user for this chat session.
         */
        String clientName = "N/A";
        final Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your name: ");
        if (scanner.hasNext()) {
            clientName = scanner.nextLine();
            System.out.println("Welcome " + clientName);
        }

        BroadcastChatClient chatClient = new BroadcastChatClient(clientName);

        chatClient.start(HOST, PORT, new BriadcastChannelInitializer(), new Consumer<Channel>() {
            @Override
            public void accept(Channel channel) {
                /*
                 * Iterate & take chat message inputs from user & then send to server.
                 */
                while (scanner.hasNext()) {
                    String input = scanner.nextLine();

                    channel.writeAndFlush("[" + chatClient.getClientName() + "]: " + input);
                    channel.flush();
                }
            }
        });
    }
}
