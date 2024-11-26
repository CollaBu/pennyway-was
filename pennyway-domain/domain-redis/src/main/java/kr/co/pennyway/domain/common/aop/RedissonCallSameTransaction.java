package kr.co.pennyway.domain.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RedissonCallSameTransaction implements CallTransaction {
    /**
     * 기존 트랜잭션 내에서 이 메서드를 실행하며, 새로운 트랜잭션을 생성하지 않는다.
     * 트랜잭션이 활성화되어 있지 않으면 예외를 발생시킨다.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, timeout = 2)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
