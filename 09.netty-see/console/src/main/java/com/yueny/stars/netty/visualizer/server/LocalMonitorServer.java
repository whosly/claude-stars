package com.yueny.stars.netty.visualizer.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yueny.stars.netty.visualizer.service.NettyMonitorService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Map;

/**
 * 本地监控服务器 - 使用LocalServerChannel接收监控数据
 * 
 * @author fengyang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalMonitorServer {
    
    private final NettyMonitorService monitorService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 跟踪应用连接和其Channel
    private final Map<String, String> channelToApp = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> appToChannels = new ConcurrentHashMap<>();
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private Channel tcpServerChannel;
    
    private static final String SOCKET_PATH = "/tmp/netty-monitor.sock";
    private static final int TCP_PORT = 19999;
    
    @PostConstruct
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        // 检查操作系统
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        
        if (!isWindows) {
            startLocalServer();
        }
        
        // 总是启动TCP服务器作为备选
        startTcpServer();
    }
    
    /**
     * 启动LocalChannel服务器（Unix系统）
     */
    private void startLocalServer() {
        try {
            // 删除已存在的socket文件
            File socketFile = new File(SOCKET_PATH);
            if (socketFile.exists()) {
                socketFile.delete();
            }
            
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(LocalServerChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            setupPipeline(ch.pipeline());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128);
            
            ChannelFuture future = bootstrap.bind(new LocalAddress(SOCKET_PATH)).sync();
            serverChannel = future.channel();
            
            log.info("Local monitor server started on socket: {}", SOCKET_PATH);
            
        } catch (Exception e) {
            log.error("Failed to start local monitor server", e);
        }
    }
    
    /**
     * 启动TCP服务器
     */
    private void startTcpServer() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            setupPipeline(ch.pipeline());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            ChannelFuture future = bootstrap.bind(new InetSocketAddress("localhost", TCP_PORT)).sync();
            tcpServerChannel = future.channel();
            
            log.info("TCP monitor server started on port: {}", TCP_PORT);
            
        } catch (Exception e) {
            log.error("Failed to start TCP monitor server", e);
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
        
        // 添加监控数据处理器
        pipeline.addLast(new MonitorDataHandler());
    }
    
    @PreDestroy
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
            if (tcpServerChannel != null) {
                tcpServerChannel.close().sync();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            
            // 删除socket文件
            File socketFile = new File(SOCKET_PATH);
            if (socketFile.exists()) {
                socketFile.delete();
            }
            
            log.info("Monitor servers stopped");
        } catch (Exception e) {
            log.error("Error stopping monitor servers", e);
        }
    }
    
    /**
     * 监控数据处理器
     */
    private class MonitorDataHandler extends ChannelInboundHandlerAdapter {
        
        private String applicationName;
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("Monitor client connected: {}", ctx.channel().id().asShortText());
            super.channelActive(ctx);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String clientId = ctx.channel().id().asShortText();
            log.info("Monitor client disconnected: {}", clientId);
            
            // 当监控代理断开连接时，标记其所有Channel为关闭状态
            if (applicationName != null) {
                Set<String> channels = appToChannels.get(applicationName);
                if (channels != null) {
                    for (String channelId : channels) {
                        monitorService.markChannelClosed(channelId);
                    }
                    appToChannels.remove(applicationName);
                    log.info("Marked {} channels as closed for disconnected application: {}", 
                            channels.size(), applicationName);
                }
            }
            
            super.channelInactive(ctx);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                String json = (String) msg;
                log.info("Received monitor data: {}", json);
                System.out.println("📥 LocalMonitorServer: Received JSON: " + json);
                
                // 解析监控消息
                MonitorMessage message = objectMapper.readValue(json, MonitorMessage.class);
                
                // 处理不同类型的消息
                switch (message.getType()) {
                    case "APP_REGISTER":
                        handleAppRegister(message);
                        break;
                    case "CHANNEL_ACTIVE":
                        handleChannelActive(message);
                        break;
                    case "CHANNEL_INACTIVE":
                        handleChannelInactive(message);
                        break;
                    case "CHANNEL_READ":
                        handleChannelRead(message);
                        break;
                    case "CHANNEL_EXCEPTION":
                        handleChannelException(message);
                        break;
                    default:
                        log.debug("Unknown message type: {}", message.getType());
                }
                
            } catch (Exception e) {
                log.warn("Failed to process monitor data: {}", e.getMessage());
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.warn("Monitor server error: {}", cause.getMessage());
            ctx.close();
        }
        
        private void handleAppRegister(MonitorMessage message) {
            this.applicationName = message.getApplicationName();
            appToChannels.putIfAbsent(applicationName, ConcurrentHashMap.newKeySet());
            log.info("Application registered: {}", message.getApplicationName());
        }
        
        private void handleChannelActive(MonitorMessage message) {
            if (message.getChannelInfo() != null) {
                String channelId = message.getChannelInfo().getChannelId();
                String appName = message.getApplicationName();
                
                System.out.println("🔄 LocalMonitorServer: Processing CHANNEL_ACTIVE for: " + channelId + " from " + appName);
                
                // 跟踪Channel和应用的关系
                channelToApp.put(channelId, appName);
                appToChannels.computeIfAbsent(appName, k -> ConcurrentHashMap.newKeySet()).add(channelId);
                
                // 转换为console模块的ChannelInfo
                com.yueny.stars.netty.visualizer.model.ChannelInfo channelInfo = 
                        convertChannelInfo(message.getChannelInfo(), message.getApplicationName());
                monitorService.registerChannel(channelInfo);
                log.info("Channel registered: {} from {}", 
                        channelInfo.getChannelId(), message.getApplicationName());
                System.out.println("✅ LocalMonitorServer: Channel registered: " + channelId + " from " + appName);
            }
        }
        
        private void handleChannelInactive(MonitorMessage message) {
            if (message.getChannelInfo() != null) {
                String channelId = message.getChannelInfo().getChannelId();
                String appName = message.getApplicationName();
                
                // 清理跟踪信息
                channelToApp.remove(channelId);
                Set<String> channels = appToChannels.get(appName);
                if (channels != null) {
                    channels.remove(channelId);
                }
                
                // 先标记为关闭状态，而不是立即移除
                monitorService.markChannelClosed(channelId);
                log.debug("Channel marked as closed: {} from {}", channelId, appName);
            }
        }
        
        private void handleChannelRead(MonitorMessage message) {
            if (message.getChannelInfo() != null) {
                // 更新Channel统计信息
                com.yueny.stars.netty.visualizer.model.ChannelInfo channelInfo = 
                        convertChannelInfo(message.getChannelInfo(), message.getApplicationName());
                monitorService.updateChannelInfo(channelInfo);
            }
        }
        
        private void handleChannelException(MonitorMessage message) {
            if (message.getChannelInfo() != null) {
                // 转换ChannelInfo并处理异常
                com.yueny.stars.netty.visualizer.model.ChannelInfo channelInfo = 
                        convertChannelInfo(message.getChannelInfo(), message.getApplicationName());
                
                // 处理异常统计
                monitorService.handleChannelException(channelInfo);
                
                log.warn("Channel exception in {}: {} - {}", 
                        message.getApplicationName(),
                        message.getChannelInfo().getChannelId(),
                        message.getChannelInfo().getErrorMessage());
            }
        }
        
        /**
         * 转换ChannelInfo格式
         */
        private com.yueny.stars.netty.visualizer.model.ChannelInfo convertChannelInfo(
                com.yueny.stars.netty.monitor.agent.model.ChannelInfo agentInfo, String appName) {
            
            com.yueny.stars.netty.visualizer.model.ChannelInfo consoleInfo = 
                    new com.yueny.stars.netty.visualizer.model.ChannelInfo();
            
            consoleInfo.setChannelId(agentInfo.getChannelId());
            consoleInfo.setRemoteAddress(agentInfo.getRemoteAddress());
            consoleInfo.setLocalAddress(agentInfo.getLocalAddress());
            consoleInfo.setActive(agentInfo.isActive());
            consoleInfo.setOpen(agentInfo.isOpen());
            consoleInfo.setWritable(agentInfo.isWritable());
            consoleInfo.setState(agentInfo.getState());
            consoleInfo.setCreateTime(agentInfo.getCreateTime() > 0 ? 
                    java.time.LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(agentInfo.getCreateTime()), 
                            java.time.ZoneId.systemDefault()) : null);
            consoleInfo.setLastActiveTime(agentInfo.getLastActiveTime() > 0 ? 
                    java.time.LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(agentInfo.getLastActiveTime()), 
                            java.time.ZoneId.systemDefault()) : null);
            consoleInfo.setBytesRead(agentInfo.getBytesRead());
            consoleInfo.setBytesWritten(agentInfo.getBytesWritten());
            consoleInfo.setMessagesRead(agentInfo.getMessagesRead());
            consoleInfo.setMessagesWritten(agentInfo.getMessagesWritten());
            consoleInfo.setEventLoopGroup(agentInfo.getEventLoopGroup());
            consoleInfo.setPipeline(agentInfo.getPipeline());
            
            // 添加错误信息
            if (agentInfo.getErrorMessage() != null) {
                consoleInfo.setErrorMessage(agentInfo.getErrorMessage());
                
                // 使用反射安全地获取错误类型和堆栈跟踪
                try {
                    java.lang.reflect.Method getErrorTypeMethod = agentInfo.getClass().getMethod("getErrorType");
                    String errorType = (String) getErrorTypeMethod.invoke(agentInfo);
                    if (errorType != null) {
                        consoleInfo.setErrorType(errorType);
                    }
                } catch (Exception e) {
                    // 方法不存在或调用失败，忽略
                }
                
                try {
                    java.lang.reflect.Method getStackTraceMethod = agentInfo.getClass().getMethod("getStackTrace");
                    String stackTrace = (String) getStackTraceMethod.invoke(agentInfo);
                    if (stackTrace != null) {
                        consoleInfo.setStackTrace(stackTrace);
                    }
                } catch (Exception e) {
                    // 方法不存在或调用失败，忽略
                }
            }
            
            // 添加应用名称信息
            consoleInfo.setApplicationName(appName);
            
            return consoleInfo;
        }
    }
    
    /**
     * 监控消息模型（内部类，避免依赖问题）
     */
    private static class MonitorMessage {
        private String type;
        private String applicationName;
        private com.yueny.stars.netty.monitor.agent.model.ChannelInfo channelInfo;
        private long timestamp;
        private Object data;
        
        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
        
        public com.yueny.stars.netty.monitor.agent.model.ChannelInfo getChannelInfo() { return channelInfo; }
        public void setChannelInfo(com.yueny.stars.netty.monitor.agent.model.ChannelInfo channelInfo) { 
            this.channelInfo = channelInfo; 
        }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}