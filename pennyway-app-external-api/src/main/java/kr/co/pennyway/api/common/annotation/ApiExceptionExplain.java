package kr.co.pennyway.api.common.annotation;

import kr.co.pennyway.common.exception.BaseErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiExceptionExplain {
    Class<? extends BaseErrorCode> value();

    String description() default "";
}
