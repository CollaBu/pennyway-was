package kr.co.pennyway.domain.domains.member.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class ChatMemberErrorException extends GlobalErrorException {
    private final ChatMemberErrorCode chatMemberErrorCode;

    public ChatMemberErrorException(ChatMemberErrorCode baseErrorCode) {
        super(baseErrorCode);
        this.chatMemberErrorCode = baseErrorCode;
    }

    public CausedBy causedBy() {
        return chatMemberErrorCode.causedBy();
    }

    public String getExplainError() {
        return chatMemberErrorCode.getExplainError();
    }
}
