package kr.co.pennyway.domain.domains.member.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatMemberErrorCode implements BaseErrorCode {
    /* 403 FORBIDDEN */
    BANNED(StatusCode.FORBIDDEN, ReasonCode.ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN, "차단된 회원입니다."),
    NOT_ADMIN(StatusCode.FORBIDDEN, ReasonCode.ACCESS_TO_RESOURCE_NOT_ALLOWED_FOR_USER_ROLE, "관리자가 아닙니다."),

    /* 404 NOT FOUND */
    NOT_FOUND(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "회원을 찾을 수 없습니다."),

    /* 409 Conflict */
    NOT_SAME_CHAT_ROOM(StatusCode.CONFLICT, ReasonCode.REQUEST_CONFLICTS_WITH_CURRENT_STATE_OF_RESOURCE, "가입한 채팅방 정보가 일치하지 않습니다."),
    ADMIN_CANNOT_LEAVE(StatusCode.CONFLICT, ReasonCode.REQUEST_CONFLICTS_WITH_CURRENT_STATE_OF_RESOURCE, "채팅방에 사용자가 남아 있다면, 채팅방 방장은 채팅방을 탈퇴할 수 없습니다."),
    ALREADY_JOINED(StatusCode.CONFLICT, ReasonCode.RESOURCE_ALREADY_EXISTS, "이미 가입한 회원입니다."),
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
