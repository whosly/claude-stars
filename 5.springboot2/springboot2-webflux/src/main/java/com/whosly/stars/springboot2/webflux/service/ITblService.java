package com.whosly.stars.springboot2.webflux.service;

import com.whosly.stars.springboot2.webflux.entry.TblEntry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Mono 和 Flux 适用于两个场景，即：
 *      Mono：实现发布者，并返回 0 或 1 个元素，即单对象
 *      Flux：实现发布者，并返回 N 个元素，即 List 列表对象
 *
 * 直接使用 Flux 和 Mono 是非阻塞写法，相当于回调方式。
 * 利用函数式可以减少了回调，因此会看不到相关接口。这恰恰是 WebFlux 的好处：集合了非阻塞 + 异步。
 */
public interface ITblService {

    List<TblEntry> queryForList();

    Flux<TblEntry> queryForListFlux();

    TblEntry queryById(Long id);

    Mono<TblEntry> queryByIdFlux(Long id);

    Long deleteById(Long id);

    Mono<Long> deleteByIdFlux(Long id);

}
