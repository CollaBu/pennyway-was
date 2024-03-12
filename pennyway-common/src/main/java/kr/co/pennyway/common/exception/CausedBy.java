package kr.co.pennyway.common.exception;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 에러 코드를 구성하는 상세 코드
 *
 * @param statusCode {@link StatusCode} 상태 코드 (3자리)
 * @param reasonCode {@link ReasonCode} 이유 코드 (1자리)
 * @param domainCode {@link DomainCode} 도메인 코드 (2자리)
 * @param fieldCode {@link FieldCode} 필드 코드 (1자리)
 *
 * - see also: {@link StatusCode}, {@link ReasonCode}, {@link DomainCode}, {@link FieldCode}
 */
public record CausedBy(
    StatusCode statusCode,
    ReasonCode reasonCode,
    DomainCode domainCode,
    FieldCode fieldCode
) {
    private static final int STATUS_CODE_MULTIPLIER = 10000;
    private static final int REASON_CODE_MULTIPLIER = 1000;
    private static final int DOMAIN_CODE_MULTIPLIER = 10;

    public CausedBy {
        Objects.requireNonNull(statusCode, "statusCode must not be null");
        Objects.requireNonNull(reasonCode, "reasonCode must not be null");
        Objects.requireNonNull(domainCode, "domainCode must not be null");
        Objects.requireNonNull(fieldCode, "fieldCode must not be null");

        if (!isValidCodes(statusCode.getCode(), reasonCode.getCode(), domainCode.getCode(), fieldCode.getCode())) {
            throw new IllegalArgumentException("Invalid bit count");
        }
    }

    /**
     * CausedBy 객체를 생성하는 정적 팩토리 메서드
     * <br/>
     * 모든 코드의 조합으로 생성된 최종 코드는 7자리의 문자열로 구성된다. (상태 코드(2자리) + 이유 코드(2자리) + 도메인 코드(1자리) + 필드 코드(2자리))
     * <br/>
     * 7자리가 아닌 경우 IllegalArgumentException을 발생시킨다.
     * @param statusCode {@link StatusCode} 상태 코드
     * @param reasonCode {@link ReasonCode} 이유 코드
     * @param domainCode {@link DomainCode} 도메인 코드
     * @param fieldCode {@link FieldCode} 필드 코드
     * @return CausedBy
     */
    public static CausedBy of(StatusCode statusCode, ReasonCode reasonCode, DomainCode domainCode, FieldCode fieldCode) {
        return new CausedBy(statusCode, reasonCode, domainCode, fieldCode);
    }

    /**
     * status code, reason code, domain code, field code를 조합하여 에러 코드를 생성한다.
     * @return String : 7자리 정수로 구성된 에러 코드
     */
    public String getCode() {
        return generateCode();
    }

    /**
     * 에러가 발생한 이유를 반환한다.
     * <br/>
     * Reason은 사전에 예외 문서에 명시한 정보를 반환한다.
     * @return String : 에러가 발생한 이유
     */
    public String getReason() {
        return reasonCode.name();
    }

    private String generateCode() {
        return String.valueOf(statusCode.getCode() * STATUS_CODE_MULTIPLIER + reasonCode.getCode() * REASON_CODE_MULTIPLIER + domainCode.getCode() * DOMAIN_CODE_MULTIPLIER + fieldCode.getCode());
    }

    private boolean isValidCodes(int statusCode, int reasonCode, int domainCode, int fieldCode) {
        return isValidDigit(statusCode, 3) && isValidDigit(reasonCode, 1) && (isValidDigit(domainCode, 1) || isValidDigit(domainCode, 2)) && isValidDigit(fieldCode, 1);
    }

    private boolean isValidDigit(int number, long expectedDigit) {
        return calcDigit(number) == expectedDigit;
    }

    private long calcDigit(int number) {
        if (number == 0) return 1;
        return Stream.iterate(number, n -> n > 0, n -> n / 10)
            .count();
    }
}
