package com.whosly.stars.springboot2.webflux.controller.handler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.annotation.Resource;

/**
 * RouterFunction，顾名思义，路由，相当于@RequestMapping，用
 * 来判断什么样的url映射到那个具体的HandlerFunction，输入为请求，输出为装在Mono里边的Handlerfunction
 */
@Configuration
public class Routers {

    @Resource
    private CHandler cHandler;
    @Resource
    private TblHandler tblHandler;

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions
                // GET  http://localhost:8060/timeFunction
                .route(RequestPredicates.GET("/timeFunction")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        cHandler::timeFunction)
                // GET APPENDER http://localhost:8060/times
                .andRoute(GET("/times"), cHandler::times)
                // GET http://localhost:8060/tbl/query/1
                .andRoute(GET("/tbl/query/{id}"), tblHandler::queryById)
                // GET http://localhost:8060/tbl/delete/1
                .andRoute(GET("/tbl/delete/{id}"), tblHandler::deleteById)
                ;
    }

}
