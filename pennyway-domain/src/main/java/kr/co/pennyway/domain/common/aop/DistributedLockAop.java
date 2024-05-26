package kr.co.pennyway.domain.common.aop;

import kr.co.pennyway.domain.common.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * {@link DistributedLock} 어노테이션을 사용한 메소드에 대한 분산 락 처리를 위한 AOP
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final CallTransactionFactory callTransactionFactory;

    @Around("@annotation(kr.co.pennyway.domain.common.aop.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                return false;
            }
            log.info("{} : Redisson Lock 진입 : {} {}", Thread.currentThread().getId(), method.getName(), key);

            return callTransactionFactory.getCallTransaction(distributedLock.needNewTransaction()).proceed(joinPoint);
        } catch (InterruptedException e) {
            throw new InterruptedException("Failed to acquire lock: " + key);
        } finally {
            try {
                log.info("{} : Redisson Lock 해제 : {} {}", Thread.currentThread().getId(), method.getName(), key);
                rLock.unlock();
            } catch (IllegalMonitorStateException ignored) {
                log.error("Redisson lock is already unlocked: {} {}", method.getName(), key);
            }
        }
    }
}
