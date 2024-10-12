package kr.co.pennyway.domain.common.redis.chatroom;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PendedChatRoomErrorCode implements BaseErrorCode {
    // 403 FORBIDDEN
    INVALID_CREATOR(StatusCode.FORBIDDEN, ReasonCode.ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN, "채팅방 정보를 생성한 사용자가 아닙니다."),

    // 404 NOT_FOUND
    NOT_FOUND(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "채팅방 정보를 찾을 수 없습니다."),
    ;

    private final StatusCode statusCode;
    private final ReasonCode reasonCode;
    private final String message;

    @Override
    public CausedBy causedBy() {
        return CausedBy.of(statusCode, reasonCode);
    }

    @Override
    public String getExplainError() throws NoSuchFieldError {
        return message;
    }
}
