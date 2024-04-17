package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthFindErrorCode implements BaseErrorCode {
	// 404 Not Found
	NOT_FOUND_USER(StatusCode.NOT_FOUND, ReasonCode.REQUESTED_RESOURCE_NOT_FOUND, "일반 회원으로 가입되지 않은 휴대폰 정보입니다.");

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
