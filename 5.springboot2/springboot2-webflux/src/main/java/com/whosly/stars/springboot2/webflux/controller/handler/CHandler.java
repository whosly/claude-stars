package com.whosly.stars.springboot2.webflux.controller.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.whosly.stars.springboot2.webflux.controller.IController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 函数式编程, HandlerFunction 相当于Controller中的具体处理方法，输入为请求，输出为装在Mono中的响应
 */
@Component
public class CHandler implements IController {

    /**
     * 返回包含时间字符串的ServerResponse
     *
     * GET  http://localhost:8060/timeFunction
     */
    public Mono<ServerResponse> timeFunction(ServerRequest request) {
        try{
            TimeUnit.MILLISECONDS.sleep(50L);
        }catch (InterruptedException e){

        }

        String date = new SimpleDateFormat("HH:mm:ss").format(new Date());

        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(
                        Mono.just(date), String.class
                );
    }



    /**
     * 每秒推送一次时间
     *
     * http://localhost:8060/times
     *
     * 我们可能会遇到一些需要网页与服务器端保持连接（起码看上去是保持连接）的需求，比如类似微信网页版的聊天类应用，比如需要频繁更新页面数据的监控系统页面或股票看盘页面。我们通常采用如下几种技术：
     *
     * 1, 短轮询：利用ajax定期向服务器请求，无论数据是否更新立马返回数据，高并发情况下可能会对服务器和带宽造成压力；
     * 2, 长轮询：利用comet不断向服务器发起请求，服务器将请求暂时挂起，直到有新的数据的时候才返回，相对短轮询减少了请求次数；
     * 3, SSE：服务端推送（Server Send Event），在客户端发起一次请求后会保持该连接，服务器端基于该连接持续向客户端发送数据，从HTML5开始加入。
     * 4, Websocket：这是也是一种保持连接的技术，并且是双向的，从HTML5开始加入，并非完全基于HTTP，适合于频繁和较大流量的双向通讯场景。
     *
     */
    public Mono<ServerResponse> times(ServerRequest request) {
        // response contentType:  MediaType.TEXT_EVENT_STREAM表示Content-Type为text/event-stream，即SSE
        return ok().contentType(MediaType.TEXT_EVENT_STREAM)
//                .body(BodyInserters.fromObject("Hi , this is SpringWebFlux~~~"));
                .body(
                    // 利用interval生成每秒一个数据的流。
                    Flux.interval(Duration.ofSeconds(1)).   // 2
                            map(l -> new SimpleDateFormat("HH:mm:ss").format(new Date())),
                    String.class
                );
    }

}
