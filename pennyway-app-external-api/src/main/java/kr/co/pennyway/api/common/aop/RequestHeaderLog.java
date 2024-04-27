package kr.co.pennyway.api.common.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestHeaderLog {
    boolean hasAuthorization() default true;

    boolean hasCookie() default false;

    enum Type {
        AUTHORIZATION,
        COOKIE
    }
}
