package kr.co.pennyway.api.common.security.jwt.refresh;

public enum RefreshTokenClaimKeys {
    USER_ID("id"),
    ROLE("role"),
    DEVICE_ID("deviceId");

    private final String value;

    RefreshTokenClaimKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
