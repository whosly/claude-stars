package com.whosly.stars.springboot2.webflux.asyn.file.wal.body;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class WALBlockBody {
    private String processId;

    private String sql;

    @Builder.Default
    private Long tid = -1L;

    public Long getTid() {
        return tid == null ? -1L : tid;
    }
}
