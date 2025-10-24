package com.flow.eda.server.instance;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Component;

/**
 * 数据库死锁重试切面
 *
 * 当检测到死锁异常时，自动重试操作
 */
@Aspect
@Component
public class DeadlockRetryAspect {

    private static final Logger logger = LoggerFactory.getLogger(DeadlockRetryAspect.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 50;

    /**
     * 拦截 FlowInstanceRepository 的所有方法，添加死锁重试逻辑
     */
    @Around("execution(* com.flow.eda.server.instance.FlowInstanceRepository.*(..))")
    public Object retryOnDeadlock(ProceedingJoinPoint pjp) throws Throwable {
        int attempts = 0;
        Throwable lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                return pjp.proceed();
            } catch (DeadlockLoserDataAccessException e) {
                lastException = e;
                attempts++;

                if (attempts < MAX_RETRIES) {
                    long delay = RETRY_DELAY_MS * attempts; // 递增延迟
                    logger.warn("检测到数据库死锁，第 {} 次重试（延迟 {}ms）: {}",
                        attempts, delay, e.getMessage());
                    Thread.sleep(delay);
                } else {
                    logger.error("数据库死锁重试 {} 次后仍然失败", MAX_RETRIES, e);
                }
            } catch (Throwable t) {
                // 非死锁异常直接抛出
                throw t;
            }
        }

        throw lastException;
    }
}
