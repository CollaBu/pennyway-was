package kr.co.infra.common.exception;

import kr.co.pennyway.common.exception.FieldCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FieldErrorCode implements FieldCode {
    ZERO(0);

    private final int code;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getFieldName() {
        return name().toLowerCase();
    }
}
