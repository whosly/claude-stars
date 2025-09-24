package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.SumUtil;

import java.nio.charset.StandardCharsets;

public interface IByte<T> {

    /**
     * IByte bean 转 bytes
     */
    byte[] toBytes();

    /**
     * val 转 bytes
     */
    default byte[] getBytes(String val){
        byte[] bytes =  val.getBytes(StandardCharsets.UTF_8);

        return bytes;
    }

    default Integer getSum(Integer... vals){
        return SumUtil.getSum(vals);
    }

}
