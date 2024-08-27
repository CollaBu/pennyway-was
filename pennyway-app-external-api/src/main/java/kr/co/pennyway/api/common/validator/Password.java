package kr.co.pennyway.api.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 어노테이션된 요소는 반드시 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해야 합니다. <br/>
 * 적어도 하나 이상의 소문자 알파벳과 숫자가 포함되어야 합니다.
 *
 * @author Yang JaeSeo
 */
@Documented
@Constraint(validatedBy = {PasswordValidator.class})
@Target({FIELD})
@Retention(RUNTIME)
public @interface Password {
    String message() default "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
