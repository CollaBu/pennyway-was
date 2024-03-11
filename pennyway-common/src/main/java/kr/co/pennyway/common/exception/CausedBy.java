package kr.co.pennyway.common.exception;

/**
 * 에러 코드를 구성하는 상세 코드
 *
 * @param statusCode {@link StatusCode} 상태 코드
 * @param reasonCode {@link ReasonCode} 이유 코드
 * @param domainCode {@link DomainCode} 도메인 코드
 * @param fieldCode {@link FieldCode} 필드 코드
 */
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
