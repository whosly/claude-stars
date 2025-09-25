package com.whosly.stars.java.reactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author fengyang
 * @date 2025-09-25 14:02:37
 * @description
 */
@Slf4j
public class ReactorDemo {
    Mono<Void> buildPipeline() {
        return buildSept(createTableMono(), "createTable", "readData")
                .thenMany(buildSept(Flux.merge(IntStream.range(0, 3) // 比如 3 现成读取
                        .mapToObj(it -> readDataFromSourceTable())
                        .collect(Collectors.toList())), "readDataFromSourceTable", "insert"))
                .doOnComplete(()-> log.info("after readDataFromSourceTable doSomething"))
                .flatMap(it -> buildSept(insertData(it), "insertData", "success"))
                .then(Mono.defer(() -> {
                    log.info("success");
                    return Mono.empty();
                }));

    }

    private Flux<Boolean> insertData(List<Object> it) {
        return Flux.defer(() -> {
            log.info("insertData");
            return Flux.just(true);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    Flux<List<Object>> readDataFromSourceTable() {
        List<List<Object>> results = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            List<Object> result = new ArrayList<>();
            result.add("1");
            result.add("2");
            results.add(result);
        }

        //        List<List<Object>> results = new ArrayList<>();
        return Flux.defer(() -> {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("readDataFromSourceTable");
            return Flux.fromStream(results.stream());
        }).subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<Void> createTableMono() {
        return Mono.defer((Supplier<Mono<Void>>) () -> {
            log.info("create table");
            return Mono.empty();
        });
    }


    public <T> Mono<T> buildSept(Mono<T> sept, String septName, String nextSeptName) {
        return sept.doOnError(e -> {
            log.error("task error on " + septName + " error:" + e.toString(), e);
        }).doOnSuccess(s -> log.info(septName + "success")).doOnCancel(() -> log.info("task cancel on" + septName));
    }


    public <T> Flux<T> buildSept(Flux<T> sept, String septName, String nextSeptName) {
        return sept.doOnError(e -> log.error("task error on {} error:{}", septName, e.toString(), e)).doOnComplete(() -> {
            log.info(septName + "success");
        }).doOnCancel(() -> log.info("task cancel on" + septName));
    }


    public static void main(String[] args) {
        ReactorDemo demo = new ReactorDemo();
        demo.buildPipeline().block();
    }
}
