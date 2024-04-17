package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class AuthFindException extends GlobalErrorException {
	private final AuthFindErrorCode errorCode;

	public AuthFindException(AuthFindErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	@Override
	public CausedBy causedBy() {
		return errorCode.causedBy();
	}

	public String getExplainError() {
		return errorCode.getExplainError();
	}
}
