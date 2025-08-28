package com.yueny.stars.netty.msgpack.domain;

import lombok.Builder;
import lombok.Data;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * 在 0.6 版本中，一定要有一个默认的构造器。否则会在 `messagePack.write(StudentInfo)` 阶段报错
 *
 * @author fengyang
 * @date 2025-08-28 10:21:30
 * @description
 */
@Data
@Builder
@Message
public class Student6Info implements Serializable {
    private int age;

    private String name;

    public Student6Info() {}

    public Student6Info(int age, String name) {
        this.age = age;
        this.name = name;
    }

}
