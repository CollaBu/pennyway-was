package kr.co.pennyway.infra.common.event;

import java.util.Objects;

public record PushCodeEvent(String phone, String code) {
    public static PushCodeEvent of(String phone, String code) {
        Objects.requireNonNull(phone);
        Objects.requireNonNull(code);

        return new PushCodeEvent(phone, code);
    }
}
