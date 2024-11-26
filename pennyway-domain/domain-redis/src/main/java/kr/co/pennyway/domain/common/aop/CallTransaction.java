package kr.co.pennyway.domain.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;

public interface CallTransaction {
    Object proceed(ProceedingJoinPoint joinPoint) throws Throwable;
}
