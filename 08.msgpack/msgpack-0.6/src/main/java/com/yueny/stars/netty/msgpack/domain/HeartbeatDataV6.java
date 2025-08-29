package com.yueny.stars.netty.msgpack.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.msgpack.annotation.Message;

/**
 * 在 0.6 版本中，一定要有一个默认的构造器。否则会在 `messagePack.write(HeartbeatData)` 阶段报错
 *
 * @author fengyang
 * @date 2025-08-28 13:55:14
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@ToString(callSuper = true)
@Message
public class HeartbeatDataV6 extends HeartbeatData {
    public HeartbeatDataV6(HeartbeatData data) {
        super(data);
    }
}
