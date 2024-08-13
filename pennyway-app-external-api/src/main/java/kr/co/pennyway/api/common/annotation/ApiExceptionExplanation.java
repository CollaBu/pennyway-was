package kr.co.pennyway.api.common.annotation;

import kr.co.pennyway.common.exception.BaseErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiExceptionExplanation {
    Class<? extends BaseErrorCode> value();

    /**
     * BaseErrorCode를 구현한 Enum 클래스의 상수명
     */
    String constant();

    String name() default "";

    String mediaType() default "application/json";

    String summary() default "";

    String description() default "";
}
