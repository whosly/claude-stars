package com.yueny.stars.netty.visualizer.example;

import com.yueny.stars.netty.visualizer.service.NettyMonitorService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 示例Netty服务器，用于测试可视化工具
 * 
 * @author fengyang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleNettyServer implements CommandLineRunner {
    
    private final NettyMonitorService monitorService;
    
    @Override
    public void run(String... args) throws Exception {
        // 在单独的线程中启动Netty服务器
        new Thread(this::startServer).start();
    }
    
    private void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // 添加监控Handler（必须在最前面）
                            pipeline.addFirst("monitor", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    monitorService.registerChannel(ctx.channel());
                                    log.info("Channel registered for monitoring: {}", ctx.channel().id().asShortText());
                                    super.channelActive(ctx);
                                }
                                
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    monitorService.unregisterChannel(ctx.channel().id().asShortText());
                                    log.info("Channel unregistered from monitoring: {}", ctx.channel().id().asShortText());
                                    super.channelInactive(ctx);
                                }
                            });
                            
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ExampleServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            ChannelFuture future = bootstrap.bind(9999).sync();
            log.info("示例Netty服务器启动成功，端口: 9999");
            
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Netty服务器启动失败", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    @ChannelHandler.Sharable
    private static class ExampleServerHandler extends SimpleChannelInboundHandler<String> {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("客户端连接: {}", ctx.channel().remoteAddress());
            ctx.writeAndFlush("欢迎连接到示例Netty服务器!\n");
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info("收到消息: {}", msg);
            ctx.writeAndFlush("Echo: " + msg + "\n");
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("客户端断开连接: {}", ctx.channel().remoteAddress());
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("处理异常", cause);
            ctx.close();
        }
    }
}