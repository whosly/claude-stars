package com.whosly.stars.netty.msgpack.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fengyang
 * @date 2025-08-28 10:21:30
 * @description
 */
@Data
@AllArgsConstructor
@Builder
public class HeartbeatData implements Serializable {
    private int type;

    private int seatId;

    private int speed;

    private String memo;

    public HeartbeatData() {}

    public HeartbeatData(HeartbeatData data) {
        this.type = data.type;
        this.seatId = data.seatId;
        this.speed = data.speed;
        this.memo = data.memo;
    }

}
