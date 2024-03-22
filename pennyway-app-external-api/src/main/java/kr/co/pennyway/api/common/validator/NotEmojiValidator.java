package kr.co.pennyway.api.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmojiValidator implements ConstraintValidator<NotEmoji, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        return !hasEmoji(value);
    }

    private boolean hasEmoji(String value) {
        return value.codePoints().anyMatch(
                codePoint -> codePoint == 0x0 || codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD
                        || (codePoint >= 0x20 && codePoint <= 0xD7FF)
                        || (codePoint >= 0xE000 && codePoint <= 0xFFFD)
                        || (codePoint >= 0x10000 && codePoint <= 0x10FFFF)
        );
    }
}
