package kr.co.pennyway.api.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 어노테이션된 요소는 반드시 이모지가 포함되어서는 안 됩니다. <br/>
 * 단, null인 경우 true를 반환합니다.
 *
 * @author Yang JaeSeo
 */
@Documented
@Constraint(validatedBy = {NotEmojiValidator.class})
@Target({FIELD})
@Retention(RUNTIME)
public @interface NotEmoji {
    String message() default "특수기호는 허용되지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
