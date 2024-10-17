package kr.co.pennyway.socket.common.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import org.springframework.messaging.simp.stomp.StompCommand;

public enum InterceptorErrorCode implements BaseErrorCode {
    // 400
    INVALID_DESTINATION(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "유효하지 않은 목적지입니다", StompCommand.SEND, StompCommand.SUBSCRIBE, StompCommand.UNSUBSCRIBE),
    INAVLID_HEADER(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST_SYNTAX, "유효하지 않은 헤더입니다", StompCommand.CONNECT),

    // 403
    UNAUTHORIZED_TO_SUBSCRIBE(StatusCode.FORBIDDEN, ReasonCode.ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN, "해당 주제에 대한 구독 권한이 없습니다", StompCommand.SUBSCRIBE, StompCommand.UNSUBSCRIBE),
    ;

    private final StatusCode statusCode;
    private final ReasonCode reasonCode;
    private final String message;
    private final StompCommand[] commands;

    InterceptorErrorCode(StatusCode statusCode, ReasonCode reasonCode, String message, StompCommand... commands) {
        this.statusCode = statusCode;
        this.reasonCode = reasonCode;
        this.message = message;
        this.commands = commands;
    }

    @Override
    public CausedBy causedBy() {
        return CausedBy.of(statusCode, reasonCode);
    }

    @Override
    public String getExplainError() throws NoSuchFieldError {
        return message;
    }

    /**
     * StompCommand가 ErrorCode에서 지원하는 명령어인지 확인하는 편의용 메서드
     *
     * @param command {@link StompCommand}
     * @return 해당 ErrorCode에서 지원하는 명령어라면 true, 아니라면 false
     */
    public boolean isSupportCommand(StompCommand command) {
        for (StompCommand c : commands) {
            if (c.equals(command)) {
                return true;
            }
        }
        return false;
    }
}