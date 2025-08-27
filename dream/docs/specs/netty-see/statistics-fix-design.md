# 统计分析页面修复设计文档

## 概述

本设计文档描述了如何修复统计分析页面中总请求数、错误率、TPS/QPS等关键指标的计算问题。主要目标是确保所有统计数据基于真实的事件数据，而不是模拟或随机生成的数据。

## 架构

### 当前架构问题
- StatisticsController直接使用模拟数据和随机值
- 缺乏与StatisticsAggregationService的有效集成
- 错误率和成功请求数基于随机计算
- TPS/QPS基于连接数估算而非实际事务

### 目标架构
```
StatisticsController
    ↓ (优先使用)
StatisticsAggregationService
    ↓ (数据来源)
TimeWindowStats (实时统计)
    ↓ (事件驱动)
NettyMonitorService (事件处理)
```

## 组件和接口

### 1. StatisticsController 重构

#### 修改的方法
- `getRealTimeStats()`: 移除模拟数据生成，直接使用StatisticsAggregationService
- `generateMockTrendData()`: 仅在开发/演示模式下使用，生产环境返回空数据

#### 新增数据验证逻辑
```java
private boolean isValidStatsData(TimeWindowStats.StatsSummary stats) {
    return stats != null && 
           stats.getTotalRequests() >= 0 && 
           stats.getErrorRate() >= 0 && 
           stats.getErrorRate() <= 100;
}
```

### 2. StatisticsAggregationService 增强

#### 新增方法
- `isDataAvailable()`: 检查是否有真实数据
- `getDataSourceStatus()`: 返回数据源状态信息

#### 修改现有逻辑
- 确保所有统计计算基于实际事件
- 改进错误率计算精度
- 添加数据一致性检查

### 3. TimeWindowStats 改进

#### 修复计算逻辑
- 请求计数：基于实际的请求-响应对
- 错误率：基于异常事件统计
- TPS/QPS：基于时间窗口内的实际事务数

#### 新增验证方法
```java
public boolean isDataConsistent() {
    return totalRequests.get() >= successfulRequests.get() + totalErrors.get();
}
```

### 4. 前端页面改进

#### 数据状态指示器
- 实时数据：绿色指示器
- 模拟数据：黄色指示器 + 警告文本
- 数据不可用：红色指示器 + 错误信息

#### 错误处理
- 优雅降级：数据不可用时显示友好提示
- 重试机制：自动重试获取数据
- 状态同步：确保所有图表使用相同的数据状态

## 数据模型

### 统计数据结构
```java
public class EnhancedStatsSummary {
    // 基础数据
    private long totalRequests;        // 实际请求数
    private long successfulRequests;   // 成功请求数  
    private long errorRequests;        // 错误请求数
    private double errorRate;          // 计算得出的错误率
    
    // 性能指标
    private double realTps;            // 基于实际事务的TPS
    private double realQps;            // 基于实际查询的QPS
    
    // 数据质量
    private boolean isRealData;        // 是否为真实数据
    private String dataSource;         // 数据来源
    private LocalDateTime dataTimestamp; // 数据时间戳
}
```

### 事件类型定义
```java
public enum StatisticsEventType {
    REQUEST_START,      // 请求开始
    REQUEST_SUCCESS,    // 请求成功
    REQUEST_ERROR,      // 请求错误
    TRANSACTION_COMMIT, // 事务提交
    QUERY_EXECUTE      // 查询执行
}
```

## 错误处理

### 数据不可用场景
1. StatisticsAggregationService未初始化
2. 没有收集到任何事件数据
3. 数据计算过程中发生异常
4. 数据一致性检查失败

### 错误处理策略
```java
public ResponseEntity<Map<String, Object>> getRealTimeStats() {
    try {
        // 1. 检查服务可用性
        if (!statisticsService.isAvailable()) {
            return createUnavailableResponse("Statistics service not available");
        }
        
        // 2. 获取真实数据
        TimeWindowStats.StatsSummary stats = statisticsService.getRealTimeStats();
        
        // 3. 验证数据一致性
        if (!isValidStatsData(stats)) {
            return createErrorResponse("Data consistency check failed");
        }
        
        // 4. 返回真实数据
        return ResponseEntity.ok(createStatsResponse(stats, true));
        
    } catch (Exception e) {
        log.error("Error getting real-time statistics", e);
        return createErrorResponse("Failed to retrieve statistics: " + e.getMessage());
    }
}
```

## 测试策略

### 单元测试
- 统计计算逻辑的准确性测试
- 边界条件测试（零请求、全部错误等）
- 数据一致性验证测试

### 集成测试
- 端到端数据流测试
- 错误场景处理测试
- 性能压力测试

### 测试数据
```java
@Test
public void testErrorRateCalculation() {
    TimeWindowStats stats = new TimeWindowStats(start, end);
    
    // 模拟10个请求，2个错误
    for (int i = 0; i < 8; i++) {
        stats.recordRequest(50.0);
        stats.recordSuccessfulRequest();
    }
    for (int i = 0; i < 2; i++) {
        stats.recordRequest(100.0);
        stats.recordError();
    }
    
    StatsSummary summary = stats.getSummary();
    assertEquals(10, summary.getTotalRequests());
    assertEquals(8, summary.getSuccessfulRequests());
    assertEquals(20.0, summary.getErrorRate(), 0.1);
}
```

## 性能考虑

### 数据缓存策略
- 实时数据：缓存1秒
- 趋势数据：缓存30秒
- 历史数据：缓存5分钟

### 计算优化
- 使用原子操作避免锁竞争
- 批量处理事件减少计算频率
- 异步计算复杂指标

### 内存管理
- 限制时间窗口数据保留量
- 定期清理过期统计数据
- 使用弱引用避免内存泄漏

## 监控和日志

### 关键指标监控
- 统计数据计算延迟
- 数据一致性检查失败率
- 服务可用性状态

### 日志策略
```java
// 数据质量日志
log.info("Statistics calculated - requests: {}, errors: {}, rate: {}%", 
         totalRequests, errorRequests, errorRate);

// 异常情况日志
log.warn("Data inconsistency detected - total: {}, success: {}, errors: {}", 
         totalRequests, successfulRequests, errorRequests);

// 性能日志
log.debug("Statistics calculation took {}ms", calculationTime);
```