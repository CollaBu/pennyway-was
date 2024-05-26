package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.BaseErrorCode;
import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.ReasonCode;
import kr.co.pennyway.common.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements BaseErrorCode {
	// 400 Bad Request
	MISSING_REQUIRED_PARAMETER(StatusCode.BAD_REQUEST, ReasonCode.MISSING_REQUIRED_PARAMETER, "필수 파라미터가 누락되었습니다."),

	// 404 Not Found
	USER_NOT_FOUND(StatusCode.NOT_FOUND, ReasonCode.RESOURCE_DELETED_OR_MOVED, "사용자를 찾을 수 없습니다.");

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
