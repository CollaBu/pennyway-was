package kr.co.infra.common.exception;

import kr.co.pennyway.common.exception.DomainCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DomainErrorCode implements DomainCode {
    ZERO(0);

    private final int code;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDomainName() {
        return name().toLowerCase();
    }
}
