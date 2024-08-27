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

    @BeforeEach
    public void setUp() {
        statusCode = StatusCode.UNPROCESSABLE_CONTENT;
        reasonCode = ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;
    }

    @Test
    @DisplayName("모두 정상적인 인자로 생성할 수 있음을 확인한다.")
    public void createWithValidArguments() {
        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode);

        // then
        assertNotNull(causedBy);
    }

    @Test
    @DisplayName("null 인자로 생성할 수 없음을 확인한다.")
    public void createWithNullArguments() {
        // given
        StatusCode statusCode = null;
        ReasonCode reasonCode = null;

        // when-then
        assertThrows(NullPointerException.class, () -> CausedBy.of(statusCode, reasonCode));
    }

    @Test
    @DisplayName("생성된 코드가 예상값과 일치하는 지 확인한다.")
    public void generateCodeWithValidArguments() {
        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode);

        // then
        assertEquals("4220", causedBy.getCode());
    }

    @Test
    @DisplayName("BAD_REQUEST - MISSING_REQUIRED_PARAMETER 에러 코드가 예상값과 일치하는 지 확인한다.")
    public void generateCodeWithTwoDigitDomainCode() {
        // given
        StatusCode statusCode = StatusCode.BAD_REQUEST;
        ReasonCode reasonCode = ReasonCode.MISSING_REQUIRED_PARAMETER;

        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode);

        // then
        assertEquals("4001", causedBy.getCode());
    }

    @Test
    @DisplayName("에러가 발생한 올바른 이유를 반환한다.")
    public void getExplainError() {
        // given
        StatusCode statusCode = StatusCode.UNPROCESSABLE_CONTENT;
        ReasonCode reasonCode = ReasonCode.REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY;

        // when
        CausedBy causedBy = CausedBy.of(statusCode, reasonCode);

        // then
        assertEquals("REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY", causedBy.getReason());
    }
}
