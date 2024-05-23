package kr.co.pennyway.domain.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * Lock 이름
     */
    String key();

    /**
     * Lock 유지 시간 (초)
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * Lock 유지 시간 (DEFAULT: 5초)
     * LOCK 획득을 위해 waitTime만큼 대기한다.
     */
    long waitTime() default 5L;

    /**
     * Lock 임대 시간 (DEFAULT: 3초)
     * LOCK 획득 이후 leaseTime이 지나면 LOCK을 해제한다.
     */
    long leaseTime() default 3L;

    /**
     * 동일한 트랜잭션에서 Lock을 획득할지 여부 (DEFAULT: false) <br/>
     */
    boolean needSameTransaction() default false;
}
