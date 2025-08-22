package com.yueny.stars.netty.monitor.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yueny.stars.netty.monitor.agent.model.ChannelInfo;
import com.yueny.stars.netty.monitor.agent.model.MonitorMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 本地监控代理 - 使用LocalChannel进行进程间通信
 * 
 * @author fengyang
 */
@Slf4j
public class LocalMonitorAgent {
    
    private final String applicationName;
    private final String socketPath;
    private final ObjectMapper objectMapper;
    private final EventLoopGroup group;
    private Channel clientChannel;
    private volatile boolean connected = false;
    private final boolean useLocalChannel;
    private final int tcpPort;
    
    public LocalMonitorAgent(String applicationName, String socketPath) {
        this.applicationName = applicationName;
        this.socketPath = socketPath;
        this.objectMapper = new ObjectMapper();
        this.group = new NioEventLoopGroup(1);
        
        // 检查操作系统，Windows使用TCP，Unix使用LocalChannel
        String os = System.getProperty("os.name").toLowerCase();
        this.useLocalChannel = !os.contains("win");
        this.tcpPort = 19999; // 监控专用端口
    }
    
    /**
     * 启动监控代理
     */
    public void start() {
        try {
            connectToMonitorServer();
        } catch (Exception e) {
            log.warn("Failed to connect to monitor server: {}", e.getMessage());
            // 启动重连任务
            scheduleReconnect();
        }
    }
    
    /**
     * 连接到监控服务器
     */
    private void connectToMonitorServer() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        
        if (useLocalChannel) {
            // Unix系统使用LocalChannel
            bootstrap.channel(LocalChannel.class)
                    .handler(new ChannelInitializer<LocalChannel>() {
                        @Override
                        protected void initChannel(LocalChannel ch) throws Exception {
                            setupPipeline(ch.pipeline());
                        }
                    });
        } else {
            // Windows系统使用TCP
            bootstrap.channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            setupPipeline(ch.pipeline());
                        }
                    });
        }
        
        try {
            ChannelFuture future;
            if (useLocalChannel) {
                future = bootstrap.connect(new LocalAddress(socketPath)).sync();
                log.info("Connected to monitor server via LocalChannel at: {}", socketPath);
            } else {
                future = bootstrap.connect(new InetSocketAddress("localhost", tcpPort)).sync();
                log.info("Connected to monitor server via TCP at: localhost:{}", tcpPort);
            }
            
            clientChannel = future.channel();
            connected = true;
            
            // 发送应用注册消息
            sendApplicationInfo();
            
        } catch (Exception e) {
            log.warn("Failed to connect to monitor server: {}", e.getMessage());
            scheduleReconnect();
        }
    }
    
    /**
     * 设置Pipeline
     */
    private void setupPipeline(ChannelPipeline pipeline) {
        // 添加长度字段编解码器
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
        pipeline.addLast(new LengthFieldPrepender(4));
        
        // 添加字符串编解码器
        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
        
        // 添加客户端处理器
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
            
            String json = objectMapper.writeValueAsString(message);
            if (clientChannel != null && clientChannel.isActive()) {
                clientChannel.writeAndFlush(json);
                log.debug("Sent application registration: {}", applicationName);
            }
        } catch (Exception e) {
            log.warn("Failed to send application info: {}", e.getMessage());
        }
    }
    
    /**
     * 发送Channel信息
     */
    public void sendChannelInfo(ChannelInfo channelInfo, String eventType) {
        if (!connected || clientChannel == null || !clientChannel.isActive()) {
            log.debug("Not connected to monitor server, skipping channel info");
            return;
        }
        
        try {
            MonitorMessage message = new MonitorMessage();
            message.setType(eventType);
            message.setApplicationName(applicationName);
            message.setChannelInfo(channelInfo);
            message.setTimestamp(System.currentTimeMillis());
            
            String json = objectMapper.writeValueAsString(message);
            clientChannel.writeAndFlush(json);
            log.debug("Sent channel info: {} - {}", eventType, channelInfo.getChannelId());
            
        } catch (Exception e) {
            log.warn("Failed to send channel info: {}", e.getMessage());
        }
    }
    
    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        group.schedule(() -> {
            if (!connected) {
                log.info("Attempting to reconnect to monitor server...");
                connectToMonitorServer();
            }
        }, 5, TimeUnit.SECONDS);
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
        log.info("Monitor agent shutdown");
    }
    
    /**
     * 监控客户端处理器
     */
    private class MonitorClientHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("Monitor client connected to server");
            connected = true;
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("Monitor client disconnected from server");
            connected = false;
            // 尝试重连
            scheduleReconnect();
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.warn("Monitor client error: {}", cause.getMessage());
            ctx.close();
        }
    }
}