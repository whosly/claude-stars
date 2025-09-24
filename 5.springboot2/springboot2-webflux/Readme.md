# 概述
基于webflux的函数式编程study, 替代传统的mvc tomcat  模式.

技术栈从命令式的、同步阻塞的【spring-webmvc + servlet + Tomcat】
变成了响应式的、异步非阻塞的【spring-webflux + Reactor + Netty】。

# test url
## TblController
* GET http://localhost:8060/list
* GET http://localhost:8060/query/2
* GET http://localhost:8060/delete/2

## DemoController
* GET  http://localhost:8060/mvc
* GET  http://localhost:8060/flux


# 启动输出
```
.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/
:: Spring Boot ::                (v2.4.4)

2022-02-09 20:31:06.689  INFO 92341 --- [           main] webflux.Application     : Starting Application using Java 1.8.0_311 on fengyang-Lenovo-ThinkBook-14p-Gen-2 with PID 92341 (/home/fengyang/workspace/yueny/gitee/study-webflux/target/classes started by fengyang in /home/fengyang/workspace/yueny/gitee/study-webflux)
2022-02-09 20:31:06.694  INFO 92341 --- [           main] webflux.Application     : No active profile set, falling back to default profiles: default
2022-02-09 20:31:07.410  INFO 92341 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=028da0f1-1c7e-3694-8225-80abb0c9dbac
2022-02-09 20:31:07.678  INFO 92341 --- [           main] c.w.s.webflux.config.AppConfiguration    : AppConfiguration load.
2022-02-09 20:31:08.465  INFO 92341 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8060
2022-02-09 20:31:08.479  INFO 92341 --- [           main] webflux.Application     : Started Application in 2.378 seconds (JVM running for 2.881)
```

## flux 的不同实现方式
### mvc 写法
com.whosly.stars.springboot2.webflux.controller#mvc

### Flux 写法
com.whosly.stars.springboot2.webflux.controller#flux

### 函数式编程写法
com.whosly.stars.springboot2.webflux.controller.handler#timeFunction

## 不同实现方式的性能压测结果比较和输出
```
每个服务均 TimeUnit.MILLISECONDS.sleep(50L)

ab -n 1000 -c 1000 <url>
－n表示请求数，－c表示并发数

ab -n 1000 -c 1000  http://localhost:8060/mvc
ab -n 1000 -c 1000 http://localhost:8060/flux
ab -n 1000 -c 1000 http://localhost:8060/timeFunction
```

### mvc 写法
```
$ ab -n 1000 -c 1000  http://localhost:8060/mvc
This is ApacheBench, Version 2.3 <$Revision: 1879490 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 100 requests
Completed 200 requests
Completed 300 requests
Completed 400 requests
Completed 500 requests
Completed 600 requests
Completed 700 requests
Completed 800 requests
Completed 900 requests
Completed 1000 requests
Finished 1000 requests

# web服务器的信息
Server Software:        
Server Hostname:        localhost
Server Port:            8060

# 关于请求的文档的相关信息，所在位置“/”，文档的大小
Document Path:          /mvc
Document Length:        28 bytes

#Concurrency Level: //并发请求数
#Time taken for tests: //整个测试持续的时间
#Complete requests: //完成的请求数
#Failed requests: //失败的请求数
#Total transferred: //整个场景中的网络传输量
#HTML transferred: //整个场景中的HTML内容传输量
#Requests per second: //吞吐率，大家最关心的指标之一，相当于 LR 中的每秒事务数，后面括号中的 mean 表示这是一个平均值
#Time per request: //用户平均请求等待时间，大家最关心的指标之二，相当于 LR 中的平均事务响应时间，后面括号中的 mean 表示这是一个平均值
#Time per request: //服务器平均请求处理时间，大家最关心的指标之三
# Transfer rate: //平均每秒网络上的流量，可以帮助排除是否存在网络流量过大导致响应时间延长的问题
Concurrency Level:      1000
Time taken for tests:   3.320 seconds
Complete requests:      1000
Failed requests:        0
Total transferred:      107000 bytes
HTML transferred:       28000 bytes
Requests per second:    301.20 [#/sec] (mean)
Time per request:       3320.065 [ms] (mean)
Time per request:       3.320 [ms] (mean, across all concurrent requests)
Transfer rate:          31.47 [Kbytes/sec] received

# 网络上消耗的时间的分解
Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0   20   4.6     20      29
Processing:    51 1627 925.2   1643    3228
Waiting:       51 1626 925.3   1643    3228
Total:         79 1647 920.7   1663    3240

# 每个请求处理时间的分布情况，50%的处理时间在多少毫秒内...，重要的是看90%的处理时间。
Percentage of the requests served within a certain time (ms)
  50%   1663
  66%   2173
  75%   2429
  80%   2583
  90%   2935
  95%   3088
  98%   3189
  99%   3190
 100%   3240 (longest request)

```

### Flux 写法
com.whosly.stars.springboot2.webflux.controller#flux
```
$ ab -n 1000 -c 1000 http://localhost:8060/flux
This is ApacheBench, Version 2.3 <$Revision: 1879490 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 100 requests
Completed 200 requests
Completed 300 requests
Completed 400 requests
Completed 500 requests
Completed 600 requests
Completed 700 requests
Completed 800 requests
Completed 900 requests
Completed 1000 requests
Finished 1000 requests


Server Software:        
Server Hostname:        localhost
Server Port:            8060

Document Path:          /flux
Document Length:        23 bytes

Concurrency Level:      1000
Time taken for tests:   3.311 seconds
Complete requests:      1000
Failed requests:        0
Total transferred:      102000 bytes
HTML transferred:       23000 bytes
Requests per second:    302.07 [#/sec] (mean)
Time per request:       3310.507 [ms] (mean)
Time per request:       3.311 [ms] (mean, across all concurrent requests)
Transfer rate:          30.09 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0   26   5.7     28      34
Processing:    51 1617 920.2   1633    3212
Waiting:       51 1617 920.3   1633    3212
Total:         84 1643 914.8   1661    3226

Percentage of the requests served within a certain time (ms)
  50%   1661
  66%   2166
  75%   2420
  80%   2573
  90%   2922
  95%   3074
  98%   3175
  99%   3178
 100%   3226 (longest request)
```

### 函数式编程写法
com.whosly.stars.springboot2.webflux.controller.handler#timeFunction
```
$ ab -n 1000 -c 1000 http://localhost:8060/timeFunction
This is ApacheBench, Version 2.3 <$Revision: 1879490 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/

Benchmarking localhost (be patient)
Completed 100 requests
Completed 200 requests
Completed 300 requests
Completed 400 requests
Completed 500 requests
Completed 600 requests
Completed 700 requests
Completed 800 requests
Completed 900 requests
Completed 1000 requests
Finished 1000 requests


Server Software:        
Server Hostname:        localhost
Server Port:            8060

Document Path:          /timeFunction
Document Length:        8 bytes

Concurrency Level:      1000
Time taken for tests:   3.291 seconds
Complete requests:      1000
Failed requests:        0
Total transferred:      78000 bytes
HTML transferred:       8000 bytes
Requests per second:    303.84 [#/sec] (mean)
Time per request:       3291.178 [ms] (mean)
Time per request:       3.291 [ms] (mean, across all concurrent requests)
Transfer rate:          23.14 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0   19   4.1     18      28
Processing:    50 1608 916.7   1624    3201
Waiting:       50 1608 916.7   1624    3201
Total:         78 1627 912.7   1642    3213

Percentage of the requests served within a certain time (ms)
  50%   1642
  66%   2147
  75%   2400
  80%   2554
  90%   2908
  95%   3060
  98%   3161
  99%   3161
 100%   3213 (longest request)
```

## SQL
```mysql
create database flux;

CREATE TABLE IF NOT EXISTS `tbl`(
        `id` INT UNSIGNED AUTO_INCREMENT,
        `title` VARCHAR(100) NOT NULL,
        `author` VARCHAR(40) NOT NULL,
        `create_date` DATE,
        PRIMARY KEY ( `id` )
        )ENGINE=InnoDB DEFAULT CHARSET=utf8;
        
INSERT INTO `tbl` VALUES (1, 'json', '123', '2019-07-27 16:01:21');
INSERT INTO `tbl` VALUES (2, 'jack jo', '123', '2019-07-24 16:01:37');
INSERT INTO `tbl` VALUES (3, 'manistal', '123', '2019-07-24 16:01:37');


-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `sex` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=armscii8;

-- ----------------------------
-- Records of t_user
-- ----------------------------
BEGIN;
INSERT INTO `t_user` VALUES (1, 'json', '123', '2019-07-27 16:01:21', 1);
INSERT INTO `t_user` VALUES (2, 'jack jo', '123', '2019-07-24 16:01:37', 1);
INSERT INTO `t_user` VALUES (3, 'manistal', '123', '2019-07-24 16:01:37', 1);
INSERT INTO `t_user` VALUES (4, 'landengdeng', '123', '2019-07-24 16:01:37', 1);
INSERT INTO `t_user` VALUES (5, 'max', '123', '2019-07-24 16:01:37', 1);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;     
```