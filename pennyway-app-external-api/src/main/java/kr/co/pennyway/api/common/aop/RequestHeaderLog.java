package kr.co.pennyway.api.common.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestHeaderLog {
    /**
     * 요청 헤더에 쿠키가 포함되어 있는 지 여부 <br/>
     * {@link org.springframework.web.bind.annotation.CookieValue}를 사용하는 컨트롤러에서는 반드시 해당 어노테이션을 명시해야 한다.
     */
    boolean hasCookie() default true;
}
