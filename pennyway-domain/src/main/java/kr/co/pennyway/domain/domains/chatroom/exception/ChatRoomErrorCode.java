package kr.co.pennyway.domain.domains.chatroom.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatRoomErrorCode implements BaseErrorCode {
    /* 400 Bad Request */
    INVALID_PASSWORD(StatusCode.BAD_REQUEST, ReasonCode.INVALID_REQUEST, "비밀번호가 일치하지 않습니다."),

    /* 409 Conflict */
    FULL_CHAT_ROOM(StatusCode.CONFLICT, ReasonCode.REQUESTED_RESPONSE_FORMAT_NOT_SUPPORTED, "채팅방 인원이 가득 찼습니다."),
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
