package com.whosly.stars.springboot2.webflux.asyn.file.wal.util;

import java.util.Arrays;

public class SumUtil {
    public static final Integer getSum(Integer... vals){
        if(vals == null || vals.length == 0){
            return 0;
        }

//        System.out.println("sum1 is " + Arrays.stream(vals).reduce(0, (a, b) -> a + b));
//        // reduce根据初始值（参数1）和累积函数（参数2）依次对数据流进行操作，
//        // 第一个值与初始值送入累积函数，后面计算结果和下一个数据流依次送入累积函数。
//        System.out.println("sum2 is " + Arrays.stream(vals).reduce(0, Integer::sum));
//        System.out.println("sum3 is " + Arrays.stream(vals).collect(Collectors.summingInt(Integer::intValue)));

        return Arrays.stream(vals).reduce(0, (a, b) -> a + b);
    }

}
