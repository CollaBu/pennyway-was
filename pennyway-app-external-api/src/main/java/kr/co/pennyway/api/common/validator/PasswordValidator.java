package kr.co.pennyway.api.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,16}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && PASSWORD_PATTERN.matcher(value).matches();
    }
}
