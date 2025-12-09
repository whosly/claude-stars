package com.whosly.stars.java.bulkheadIsolation.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于Netty的物理隔离
 */
@Configuration
public class BulkheadIsolationConfig {
    /**
     * 银行服务使用完全独立的连接池和线程池
     */
    @Bean
    public HttpClient bankHttpClient() {
        return HttpClient
                // 每个外部服务独立连接池
                .create(ConnectionProvider.builder("bank-pool")
                        .maxConnections(100)      // 最大连接数
                        .pendingAcquireTimeout(Duration.ofSeconds(1))
                        .evictInBackground(Duration.ofSeconds(120))
                        .build())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(3000, TimeUnit.MILLISECONDS))
                )
                // 独立的IO线程组
                .runOn(LoopResources.create("bank-io", 4, true));
    }

    /**
     * 微信支付使用完全独立的连接池和线程池
     */
    @Bean
    public HttpClient wechatHttpClient() {
        return HttpClient.create(ConnectionProvider.builder("wechat-pool")
                        .maxConnections(50)
                        .build())
                .runOn(LoopResources.create("wechat-io", 2, true));
    }
}
