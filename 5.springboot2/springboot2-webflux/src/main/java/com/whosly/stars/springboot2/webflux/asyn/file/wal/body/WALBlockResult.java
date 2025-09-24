package com.whosly.stars.springboot2.webflux.asyn.file.wal.body;

import lombok.Builder;
import lombok.Data;

/**
 * 响应对象
 */
@Data
@Builder
public class WALBlockResult {

    /**
     * 当前bolck 数据分配的 lsn
     */
    private Long lsn;

}
