package com.yueny.stars.netty.monitor.agent.core;

import com.yueny.stars.netty.monitor.agent.model.ChannelInfo;
import com.yueny.stars.netty.monitor.agent.model.MonitorMessage;
import com.yueny.stars.netty.monitor.agent.util.JsonUtil;
import com.yueny.stars.netty.monitor.agent.util.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 监控代理核心类
 * 
 * @author fengyang
 */
public class MonitorAgent {
    
    private static final Logger logger = Logger.getLogger(MonitorAgent.class);
    
    private final String applicationName;
    private final String host;
    private final int port;
    private final EventLoopGroup group;
    private Channel clientChannel;
    private volatile boolean connected = false;
    private static MonitorAgent instance;
    
    private MonitorAgent(String applicationName, String host, int port) {
        this.applicationName = applicationName;
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup(1);
    }
    
    /**
     * 初始化监控代理
     */
    public static synchronized void initialize(String applicationName, String host, int port) {
        if (instance == null) {
            instance = new MonitorAgent(applicationName, host, port);
            instance.start();
            
            // 添加JVM关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (instance != null) {
                    instance.shutdown();
                }
            }));
        }
    }
    
    /**
     * 获取监控代理实例
     */
    public static MonitorAgent getInstance() {
        return instance;
    }
    
    /**
     * 获取监控Handler
     */
    public static ChannelHandler getMonitorHandler() {
        if (instance == null) {
            logger.info("MonitorAgent: Instance is null, returning NoOpHandler");
            return new NoOpHandler();
        }
        
        // 即使还没有连接，也返回真正的MonitorHandler
        // 这样可以确保Channel事件被捕获，即使监控数据暂时无法发送
        logger.info("MonitorAgent: Returning MonitorHandler (connected:  %s)", instance.connected);
        return new MonitorHandler(instance);
    }
    
    /**
     * 启动监控代理
     */
    private void start() {
        try {
            connectToMonitorServer();
        } catch (Exception e) {
            logger.warn("Failed to connect to monitor server: %s", e.getMessage());
            scheduleReconnect();
        }
    }
    
    /**
     * 连接到监控服务器
     */
    private void connectToMonitorServer() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        setupPipeline(ch.pipeline());
                    }
                });
        
        try {
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
            future.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    clientChannel = channelFuture.channel();
                    connected = true;
                    logger.info("Successfully connected to monitor server at %s:%d", host, port);
                    
                    // 发送应用注册消息
                    sendApplicationInfo();
                } else {
                    logger.warn("Failed to connect to monitor server: %s", channelFuture.cause().getMessage());

                    scheduleReconnect();
                }
            });
            
        } catch (Exception e) {
            logger.warn("Failed to connect to monitor server: %s", e.getMessage());
            scheduleReconnect();
        }
    }
    
    /**
     * 设置Pipeline
     */
    private void setupPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast(new MonitorClientHandler());
    }
    
    /**
     * 发送应用信息
     */
    private void sendApplicationInfo() {
        try {
            MonitorMessage message = new MonitorMessage();
            message.setType("APP_REGISTER");
            message.setApplicationName(applicationName);
            message.setTimestamp(System.currentTimeMillis());
            
            String json = JsonUtil.builder()
                    .put("type", message.getType())
                    .put("applicationName", message.getApplicationName())
                    .put("timestamp", message.getTimestamp())
                    .build();
            
            if (clientChannel != null && clientChannel.isActive()) {
                clientChannel.writeAndFlush(json);
                logger.trace("Monitor Agent: Sent application registration for: %s", applicationName);
            } else {
                logger.warn("Monitor Agent Cannot send application info - channel not active");
            }
        } catch (Exception e) {
            logger.warn("Monitor Agent: Failed to send application info: %s", e.getMessage());
        }
    }
    
    /**
     * 发送Channel信息
     */
    public void sendChannelInfo(ChannelInfo channelInfo, String eventType) {
        if (!connected || clientChannel == null || !clientChannel.isActive()) {
            logger.info("Not connected to monitor server, skipping %s channel info for %s.", eventType, (channelInfo != null ? channelInfo.getChannelId() : "unknown"));
            return;
        }
        
        try {
            String json = JsonUtil.builder()
                    .put("type", eventType)
                    .put("applicationName", applicationName)
                    .put("channelInfo", channelInfo)
                    .put("timestamp", System.currentTimeMillis())
                    .build();

            logger.debug("MonitorAgent: Sending JSON(substring 20): %s...", json.substring(0, Math.min(20, json.length())));
            clientChannel.writeAndFlush(json);
            logger.debug("Sent channel info: %s for channel %s", eventType, channelInfo.getChannelId());
        } catch (Exception e) {
            logger.warn("Failed to send channel info: %s", e.getMessage());
        }
    }
    
    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        group.schedule(() -> {
            if (!connected) {
                logger.debug("Attempting to reconnect to monitor server...");
                connectToMonitorServer();
            }
        }, 5, TimeUnit.SECONDS);
    }
    
    /**
     * 检查是否已连接到监控服务器
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * 关闭监控代理
     */
    public void shutdown() {
        connected = false;
        if (clientChannel != null) {
            clientChannel.close();
        }
        group.shutdownGracefully();
        logger.info("Monitor agent shutdown");
    }
    
    /**
     * 监控客户端处理器
     */
    private class MonitorClientHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("Monitor client connected to server at %s:%s", host, port);
            connected = true;
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            logger.warn("Monitor client disconnected from server");
            connected = false;
            scheduleReconnect();
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.warn("Monitor client error: %s", cause.getMessage());
            ctx.close();
        }
    }
    
    /**
     * 空操作Handler，用于未初始化时
     */
    private static class NoOpHandler extends ChannelInboundHandlerAdapter {
        // 什么都不做
    }
}