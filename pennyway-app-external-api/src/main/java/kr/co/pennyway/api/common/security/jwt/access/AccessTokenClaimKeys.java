package kr.co.pennyway.api.common.security.jwt.access;

public enum AccessTokenClaimKeys {
    USER_ID("userId"),
    ROLE("role");

    private final String value;

    AccessTokenClaimKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
