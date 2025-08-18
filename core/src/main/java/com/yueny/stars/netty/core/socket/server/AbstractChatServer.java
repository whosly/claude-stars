package com.yueny.stars.netty.core.socket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * @author fengyang
 * @date 2025-08-18 14:19:30
 * @description
 */
public abstract class AbstractChatServer implements ISocketServer {
    /**
     * bossGroup 用来接收进来的连接
     */
    private final EventLoopGroup bossGroup;
    /**
     * workerGroup 用来处理已经被接收的连接，一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
     */
    private final NioEventLoopGroup workerGroup;
    private final ServerBootstrap bootstrap;

    protected AbstractChatServer() {
        /*
         * Configure the server.
         */

        // Create boss & worker groups. Boss accepts connections from client. Worker handles further communication through connections.
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        this.bootstrap = new ServerBootstrap();
        this.bootstrap
                // Set boss & worker groups
                .group(bossGroup, workerGroup);
    }

    @Override
    public ServerBootstrap start(int port, ChannelInitializer<SocketChannel> initializer, Consumer<String> consumer) throws Exception {
        try {
            this.bootstrap
                    // Use NIO to accept new connections.
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(initializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Start the server.
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("Chat Server started. Ready to accept chat clients.");

            if(consumer != null) {
                consumer.accept("");
            }

            // Wait until the server socket is closed.
            future.channel().closeFuture().sync();
        } finally {
            stop();
        }

        return this.bootstrap;
    }

    @Override
    public void stop() throws Exception {
        // Shut down all event loops to terminate all threads.
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully().sync();
    }
}
