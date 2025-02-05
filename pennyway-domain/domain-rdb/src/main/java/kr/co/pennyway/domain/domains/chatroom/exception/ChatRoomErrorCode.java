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

    /* 404 Not Found */
    NOT_FOUND_CHAT_ROOM(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    /* 409 Conflict */
    FULL_CHAT_ROOM(StatusCode.CONFLICT, ReasonCode.REQUESTED_RESPONSE_FORMAT_NOT_SUPPORTED, "채팅방 인원이 가득 찼습니다."),
    AREADY_JOINED(StatusCode.CONFLICT, ReasonCode.RESOURCE_ALREADY_EXISTS, "이미 채팅방에 참여한 사용자입니다.");

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
