package com.yueny.stars.netty.msgpack.heartbeat.client;

import com.yueny.stars.netty.msgpack.domain.HeartbeatData;
import com.yueny.stars.netty.msgpack.domain.TypeData;
import com.yueny.stars.netty.msgpack.heartbeat.code.MsgPackDecoder;
import com.yueny.stars.netty.msgpack.heartbeat.code.MsgPackEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author fengyang
 * @date 2025-08-28 17:37:50
 * @description
 */
public class Client {

    public static void main(String[] args) throws Exception {
        Client client = new Client(2, TimeUnit.SECONDS);
        client.start("localhost", 8883);

        client.sendData();
    }

    private final ScheduledExecutorService scheduler;
    private final long period;
    private final TimeUnit unit;
    private final AtomicBoolean isRunning;
    private ScheduledFuture<?> future;

    private NioEventLoopGroup group = new NioEventLoopGroup(4);
    private Channel channel;
    private Bootstrap bootstrap;

    public Client(long period, TimeUnit unit) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ResilientScheduledExecutorTask-Thread");
            t.setDaemon(true); // 使用守护线程
            return t;
        });

        this.period = period;
        this.unit = unit;
        this.isRunning = new AtomicBoolean(false);
    }

    public void start(final String host, final int port) {
        try {
            this.bootstrap = new Bootstrap();
            this.bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();

//                            p.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, -4, 0));
//                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
                            p.addLast(new MsgPackDecoder());
                            p.addLast(new MsgPackEncoder());
                            p.addLast(new ClientHandler());
                        }
                    });

            if (isRunning.compareAndSet(false, true)) {
                this.future = scheduler.scheduleAtFixedRate(() -> {
                    try {
                        doConnectSchedule(host, port);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }, 0, period, unit);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重连机制,每隔2s重新连接一次服务器
     */
    protected void doConnectSchedule(String host, int port) throws InterruptedException {
        if (isChannelActive()) {
            return;
        }

        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener((ChannelFutureListener) futureListener -> {
            if (futureListener.isSuccess()) {
                channel = futureListener.channel();
                System.out.println("Connect to server successfully!");
            } else {
                System.err.println("Failed to connect to server, try connect after 2s");
            }
        });
    }

    /**
     * 发送数据 每隔2秒发送一次
     */
    public void sendData() throws Exception {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10000; i++) {
            if (isChannelActive()) {
                HeartbeatData heartbeatData = new HeartbeatData();
                heartbeatData.setType(TypeData.CUSTOME);
                heartbeatData.setSeatId(i);
                heartbeatData.setSpeed(i%20);

                channel.writeAndFlush(heartbeatData);
                System.out.println("client 发送数据:" + heartbeatData);
            }else{
                // channel 没有准备好。再次尝试
                Thread.sleep(random.nextInt(500));
            }

            // 模拟业务数据的发送间隔
            Thread.sleep(random.nextInt(100));
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (future != null) {
                future.cancel(false);
            }
            scheduler.shutdown();
        }
    }

    /**
     * 判断channel是否处于活动状态
     */
    public boolean isChannelActive() {
        return this.channel != null && this.channel.isActive();
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
