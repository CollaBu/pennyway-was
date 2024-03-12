package kr.co.pennyway.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CausedByTest {
    private StatusCode statusCode;
    private ReasonCode reasonCode;
    private DomainCode domainCode;
    private FieldCode fieldCode;

    @BeforeEach
    public void setUp() {
        statusCode = StatusCode.UNPROCESSABLE_CONTENT;
        reasonCode = ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;
        domainCode = DomainBitCode.USER;
        fieldCode = UserFieldCode.NAME;
    }

    @Test
    @DisplayName("모두 정상적인 인자로 생성할 수 있음을 확인한다.")
    public void createWithValidArguments() {
        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode, domainCode, fieldCode);

        // then
        assertNotNull(causedBy);
    }

    @Test
    @DisplayName("null 인자로 생성할 수 없음을 확인한다.")
    public void createWithNullArguments() {
        // given
        StatusCode statusCode = null;
        ReasonCode reasonCode = null;
        DomainCode domainCode = null;
        FieldCode fieldCode = null;

        // when-then
        assertThrows(NullPointerException.class, () -> CausedBy.of(statusCode, reasonCode, domainCode, fieldCode));
    }

    @Test
    @DisplayName("상태 코드(3), 이유 코드(1), 도메인 코드(2), 필드 코드(1)가 아니면 생성할 수 없음을 확인한다.")
    public void createWithInvalidBitCountArguments() {
        // given
        FieldCode fieldCode = UserFieldCode.INVALID;

        // when-then
        assertThrows(IllegalArgumentException.class, () -> CausedBy.of(statusCode, reasonCode, domainCode, fieldCode));
    }

    @Test
    @DisplayName("생성된 코드가 예상값과 일치하는 지 확인한다.")
    public void generateCodeWithValidArguments() {
        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode, domainCode, fieldCode);

        // then
        assertEquals("4220013", causedBy.getCode());
    }

    @Test
    @DisplayName("두 자리 수의 도메인 코드가 예상값과 일치하는 지 확인한다.")
    public void generateCodeWithTwoDigitDomainCode() {
        // given
        StatusCode statusCode = StatusCode.BAD_REQUEST;
        ReasonCode reasonCode = ReasonCode.MISSING_REQUIRED_PARAMETER;
        DomainCode domainCode = DomainBitCode.ORDER;
        FieldCode fieldCode = UserFieldCode.ZERO;

        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode, domainCode, fieldCode);

        // then
        assertEquals("4001130", causedBy.getCode());
    }

    @Test
    @DisplayName("에러가 발생한 올바른 이유를 반환한다.")
    public void getExplainError() {
        // given
        StatusCode statusCode = StatusCode.UNPROCESSABLE_CONTENT;
        ReasonCode reasonCode = ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;
        DomainCode domainCode = DomainBitCode.USER;
        FieldCode fieldCode = UserFieldCode.NAME;

        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode, domainCode, fieldCode);

        // then
        assertEquals("REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY", causedBy.reasonCode().name());
    }

    private enum DomainBitCode implements DomainCode {
        ZERO(0), USER(1), PRODUCT(2),
        ORDER(13);

        private final int code;

        DomainBitCode(int code) {
            this.code = code;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getDomainName() {
            return name().toLowerCase();
        }
    }

    private enum UserFieldCode implements FieldCode {
        ZERO(0), ID(1), PASSWORD(2), NAME(3),
        INVALID(10)
        ;

        private final int code;

        UserFieldCode(int code) {
            this.code = code;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getFieldName() {
            return name().toLowerCase();
        }
    }
}
