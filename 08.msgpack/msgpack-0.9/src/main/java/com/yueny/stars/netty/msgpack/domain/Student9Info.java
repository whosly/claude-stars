package com.yueny.stars.netty.msgpack.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 在 0.9 版本中，已经没有了注解 `org.msgpack.annotation.Message`， 且默认构造函数不再要求必须存在
 *
 * @author fengyang
 * @date 2025-08-28 12:08:51
 * @description
 */
@Data
@Builder
public class Student9Info implements Serializable {
    private int age;

    private String name;

}
