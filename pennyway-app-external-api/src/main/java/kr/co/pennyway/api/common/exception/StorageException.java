package kr.co.pennyway.api.common.exception;

import kr.co.pennyway.common.exception.CausedBy;
import kr.co.pennyway.common.exception.GlobalErrorException;

public class StorageException extends GlobalErrorException {
	private final StorageErrorCode errorCode;

	public StorageException(StorageErrorCode errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	@Override
	public CausedBy causedBy() {
		return errorCode.causedBy();
	}

	public StorageErrorCode getErrorCode() {
		return errorCode;
	}
}
