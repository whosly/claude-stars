package com.yueny.stars.netty.msgpack.domain;

/**
 * @author fengyang
 * @date 2025-08-28 13:31:51
 * @description
 */
public interface TypeData {
    // type 模式
    byte PING = 1;

    byte PONG = 2;

    byte CUSTOME = 3;

    // SEAT
    byte PING_SEAT = 100;

    byte PONG_SEAT = 101;

    // SERVER
    byte SERVER_RESPONSE = 102;

    byte SERVER_RESISTANT = 103;
}
