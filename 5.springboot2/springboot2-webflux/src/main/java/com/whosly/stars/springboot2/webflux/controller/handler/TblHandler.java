package com.whosly.stars.springboot2.webflux.controller.handler;

import com.whosly.stars.springboot2.webflux.controller.IController;
import com.whosly.stars.springboot2.webflux.entry.TblEntry;
import com.whosly.stars.springboot2.webflux.service.ITblService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * 演示各种请求入参情况
 */
@Component
public class TblHandler implements IController {

    @Autowired
    private ITblService tblService;

    /**
     * id 信息查询
     *
     * http://localhost:port/tbl/query/{id}
     * --> http://localhost:8060/tbl/query/1
     */
    public Mono<ServerResponse> queryById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        Mono<TblEntry> entry = Mono.justOrEmpty(tblService.queryById(id));

        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(entry, TblEntry.class);
    }

    /**
     * http://localhost:port/tbl/delete/{id}
     */
    public Mono<ServerResponse> deleteById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(tblService.deleteById(id)), Integer.class);
    }

}
