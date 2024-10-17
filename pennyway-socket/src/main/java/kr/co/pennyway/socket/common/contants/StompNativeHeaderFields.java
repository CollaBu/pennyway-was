package kr.co.pennyway.socket.common.contants;

public enum StompNativeHeaderFields {
    DEVICE_ID("device-id"),
    DEVICE_NAME("device-name"),
    ;

    private final String value;

    StompNativeHeaderFields(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
