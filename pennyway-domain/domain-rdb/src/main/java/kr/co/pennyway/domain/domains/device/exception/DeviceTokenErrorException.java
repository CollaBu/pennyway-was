package kr.co.pennyway.domain.domains.device.exception;

import kr.co.pennyway.common.exception.GlobalErrorException;

public class DeviceTokenErrorException extends GlobalErrorException {
    private final DeviceTokenErrorCode deviceTokenErrorCode;

    public DeviceTokenErrorException(DeviceTokenErrorCode deviceTokenErrorCode) {
        super(deviceTokenErrorCode);
        this.deviceTokenErrorCode = deviceTokenErrorCode;
    }

    public String getExplainError() {
        return deviceTokenErrorCode.getExplainError();
    }

    public String getErrorCode() {
        return deviceTokenErrorCode.name();
    }
}
