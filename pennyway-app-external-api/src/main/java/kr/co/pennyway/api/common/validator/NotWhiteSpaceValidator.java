package kr.co.pennyway.api.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotWhiteSpaceValidator implements ConstraintValidator<NotWhiteSpace, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !hasWhiteSpace(value);
    }

    private boolean hasWhiteSpace(String value) {
        return value.chars().anyMatch(Character::isWhitespace);
    }
}