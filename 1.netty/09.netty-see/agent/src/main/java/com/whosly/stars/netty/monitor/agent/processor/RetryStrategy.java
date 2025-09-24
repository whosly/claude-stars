package com.whosly.stars.netty.monitor.agent.processor;

import com.whosly.stars.netty.monitor.agent.util.Logger;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 重试策略接口和实现
 * 定义不同的重试策略，支持固定间隔、指数退避等
 * 
 * @author fengyang
 */
public interface RetryStrategy {
    
    /**
     * 计算下次重试的延迟时间
     * 
     * @param attemptCount 当前重试次数（从1开始）
     * @param baseInterval 基础间隔时间（毫秒）
     * @return 延迟时间（毫秒）
     */
    long calculateDelay(int attemptCount, long baseInterval);
    
    /**
     * 获取策略名称
     */
    String getName();
    
    /**
     * 固定间隔重试策略
     */
    class FixedInterval implements RetryStrategy {
        private static final Logger logger = Logger.getLogger(FixedInterval.class);
        
        @Override
        public long calculateDelay(int attemptCount, long baseInterval) {
            logger.debug("固定间隔重试策略: 第%d次重试, 延迟%d ms", attemptCount, baseInterval);
            return baseInterval;
        }
        
        @Override
        public String getName() {
            return "FixedInterval";
        }
    }
    
    /**
     * 指数退避重试策略
     */
    class ExponentialBackoff implements RetryStrategy {
        private static final Logger logger = Logger.getLogger(ExponentialBackoff.class);
        
        private final double multiplier;
        private final long maxDelay;
        
        public ExponentialBackoff() {
            this(2.0, 30000); // 默认2倍增长，最大30秒
        }
        
        public ExponentialBackoff(double multiplier, long maxDelay) {
            this.multiplier = multiplier;
            this.maxDelay = maxDelay;
        }
        
        @Override
        public long calculateDelay(int attemptCount, long baseInterval) {
            long delay = (long) (baseInterval * Math.pow(multiplier, attemptCount - 1));
            delay = Math.min(delay, maxDelay);
            
            logger.debug("指数退避重试策略: 第%d次重试, 延迟%d ms (倍数: %.1f, 最大: %d ms)", 
                    attemptCount, delay, multiplier, maxDelay);
            
            return delay;
        }
        
        @Override
        public String getName() {
            return "ExponentialBackoff";
        }
    }
    
    /**
     * 线性增长重试策略
     */
    class LinearBackoff implements RetryStrategy {
        private static final Logger logger = Logger.getLogger(LinearBackoff.class);
        
        private final long increment;
        private final long maxDelay;
        
        public LinearBackoff() {
            this(1000, 10000); // 默认每次增加1秒，最大10秒
        }
        
        public LinearBackoff(long increment, long maxDelay) {
            this.increment = increment;
            this.maxDelay = maxDelay;
        }
        
        @Override
        public long calculateDelay(int attemptCount, long baseInterval) {
            long delay = baseInterval + (attemptCount - 1) * increment;
            delay = Math.min(delay, maxDelay);
            
            logger.debug("线性增长重试策略: 第%d次重试, 延迟%d ms (增量: %d ms, 最大: %d ms)", 
                    attemptCount, delay, increment, maxDelay);
            
            return delay;
        }
        
        @Override
        public String getName() {
            return "LinearBackoff";
        }
    }
    
    /**
     * 随机抖动重试策略
     */
    class RandomJitter implements RetryStrategy {
        private static final Logger logger = Logger.getLogger(RandomJitter.class);
        
        private final RetryStrategy baseStrategy;
        private final double jitterFactor;
        
        public RandomJitter(RetryStrategy baseStrategy) {
            this(baseStrategy, 0.1); // 默认10%抖动
        }
        
        public RandomJitter(RetryStrategy baseStrategy, double jitterFactor) {
            this.baseStrategy = baseStrategy;
            this.jitterFactor = Math.max(0.0, Math.min(1.0, jitterFactor));
        }
        
        @Override
        public long calculateDelay(int attemptCount, long baseInterval) {
            long baseDelay = baseStrategy.calculateDelay(attemptCount, baseInterval);
            
            // 添加随机抖动
            long jitterRange = (long) (baseDelay * jitterFactor);
            long jitter = ThreadLocalRandom.current().nextLong(-jitterRange, jitterRange + 1);
            long finalDelay = Math.max(0, baseDelay + jitter);
            
            logger.debug("随机抖动重试策略: 第%d次重试, 基础延迟%d ms, 抖动%d ms, 最终延迟%d ms", 
                    attemptCount, baseDelay, jitter, finalDelay);
            
            return finalDelay;
        }
        
        @Override
        public String getName() {
            return "RandomJitter(" + baseStrategy.getName() + ")";
        }
    }
}