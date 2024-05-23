package kr.co.pennyway.domain.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RedissonCallNewTransaction implements CallTransaction {
    /**
     * 다른 트랜잭션이 실행 중인 경우에도 새로운 트랜잭션을 생성하여 이 메서드를 실행한다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 2)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return null;
    }
}
