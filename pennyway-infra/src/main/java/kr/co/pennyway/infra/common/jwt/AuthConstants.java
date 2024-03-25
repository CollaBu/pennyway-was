package kr.co.pennyway.infra.common.jwt;

import lombok.Getter;

@Getter
public enum AuthConstants {
    AUTHORIZATION("Authorization"), TOKEN_TYPE("Bearer ");

    private final String value;

    AuthConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AuthConstants(value=" + this.value + ")";
    }
}
