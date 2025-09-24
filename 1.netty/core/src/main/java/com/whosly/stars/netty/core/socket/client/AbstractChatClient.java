package com.whosly.stars.netty.core.socket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.function.Consumer;

/**
 * @author fengyang
 * @date 2025-08-18 14:34:03
 * @description
 */
public class AbstractChatClient implements IClient {
    /**
     * 客户端用户名
     */
    private String clientName;

    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;

    protected AbstractChatClient(String clientName) {
        this.clientName = clientName;

        /*
         * Configure the client.
         */

        // Since this is client, it doesn't need boss group. Create single group.
        workerGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap
                // Set EventLoopGroup to handle all eventsf for client.
                .group(workerGroup);
    }

    @Override
    public <R> boolean start(String serverHost, int serverPort, ChannelInitializer<SocketChannel> channelInitializer, Consumer<Channel> consumer) throws Exception {
        try {
            bootstrap
                    // Use NIO to accept new connections.
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(channelInitializer);

            // Start the client.
            ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();

            if(consumer != null) {
                // 得到channel
                Channel channel = future.sync().channel();
                consumer.accept(channel);
            }

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } finally {
            stop();
        }

        return true;
    }

    @Override
    public void stop() throws Exception {
        // Shut down the event loop to terminate all threads.
        workerGroup.shutdownGracefully();
    }

    public String getClientName() {
        return clientName;
    }
}
