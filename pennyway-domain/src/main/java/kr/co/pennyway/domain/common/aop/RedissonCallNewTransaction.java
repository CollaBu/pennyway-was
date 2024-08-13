package kr.co.pennyway.domain.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RedissonCallNewTransaction implements CallTransaction {
    /**
     * 다른 트랜잭션이 실행 중인 경우에도 새로운 트랜잭션을 생성하여 이 메서드를 실행한다.
     * 동시성 환경에서 데이터 정합성을 보장하기 위해 트랜잭션 커밋 이후 락이 해제된다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
