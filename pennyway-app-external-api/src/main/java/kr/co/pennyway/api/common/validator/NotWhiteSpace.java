package kr.co.pennyway.api.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 어노테이션된 요소는 반드시 공백문자가 포함되어서는 안 됩니다. <br/>
 * 단, null인 경우 false를 반환합니다.
 *
 * @author Yang JaeSeo
 * @see Character#isWhitespace(char)
 */
@Documented
@Constraint(validatedBy = {NotWhiteSpaceValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface NotWhiteSpace {
    String message() default "공백 문자는 허용되지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
