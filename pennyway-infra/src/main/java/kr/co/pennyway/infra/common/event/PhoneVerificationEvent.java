package kr.co.pennyway.infra.common.event;

import java.util.Objects;

public record PhoneVerificationEvent(String phone, String code) {
    public static PhoneVerificationEvent of(String phone, String code) {
        Objects.requireNonNull(phone);
        Objects.requireNonNull(code);

        return new PhoneVerificationEvent(phone, code);
    }
}
