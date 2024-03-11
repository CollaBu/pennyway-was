package kr.co.pennyway.common.exception;

public record CausedBy(
    StatusCode statusCode,
    ReasonCode reasonCode,
    DomainCode domainCode,
    FieldCode fieldCode
) {
    public static CausedBy valueOf(StatusCode statusCode, ReasonCode reasonCode, DomainCode domainCode, FieldCode fieldCode) {
        return new CausedBy(statusCode, reasonCode, domainCode, fieldCode);
    }

    public String getCode() {
        return String.valueOf(statusCode.getCode() * 10000 + reasonCode.getCode() * 1000 + domainCode.getCode() * 10 + fieldCode.getCode());
    }

    public String getReason() {
        return reasonCode.name();
    }
}
