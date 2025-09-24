package com.whosly.stars.springboot2.webflux.dao;

import com.whosly.stars.springboot2.webflux.entry.TblEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrudRepository接口,主要是完成一些增删改查的操作。注意：CrudRepository接口继承了Repository接口
 * PagingAndSortingRepository接口的使用. 该接口提供了分页与排序的操作，注意：该接口继承了CrudRepository接口
 * JpaRepository接口, 该接口继承了PagingAndSortingRepository。对继承的父接口中方法的返回值进行适配。
 *
 * JPASpecificationExecutor接口.该接口主要是提供了多条件查询的支持，并且可以在查询中添加排序与分页。注意JPASpecificationExecutor是单独存在的。完全独立
 */
public interface TblRepository extends
        JpaRepository<TblEntry, Long> {
    //.
}
