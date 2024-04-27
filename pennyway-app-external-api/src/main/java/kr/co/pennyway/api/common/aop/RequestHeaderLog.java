package kr.co.pennyway.api.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestHeaderLog {
    boolean hasAuthorization() default true;

    boolean hasCookie() default false;

    enum Type {
        AUTHORIZATION,
        COOKIE
    }
}
