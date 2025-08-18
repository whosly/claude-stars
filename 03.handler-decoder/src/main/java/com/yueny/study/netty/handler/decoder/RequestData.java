package com.yueny.study.netty.handler.decoder;

import lombok.Builder;
import lombok.Data;

/**
 * @author fengyang
 * @date 2025-08-18 09:44:10
 * @description
 */
@Data
@Builder
public class RequestData {
    private int intValue;

    private String stringValue;


}
