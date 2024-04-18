package kr.co.pennyway.domain.domains.device.exception;

import kr.co.pennyway.common.exception.GlobalErrorException;

public class DeviceErrorException extends GlobalErrorException {
    private final DeviceErrorCode deviceErrorCode;

    public DeviceErrorException(DeviceErrorCode deviceErrorCode) {
        super(deviceErrorCode);
        this.deviceErrorCode = deviceErrorCode;
    }

    public String getExplainError() {
        return deviceErrorCode.getExplainError();
    }

    public String getErrorCode() {
        return deviceErrorCode.name();
    }
}
