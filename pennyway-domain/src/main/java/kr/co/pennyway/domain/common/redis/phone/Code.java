package kr.co.pennyway.domain.common.redis.phone;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Code {
    SIGN_UP("signup"), FIND_USERNAME("username"), FIND_PASSWORD("password");

    private final String prefix;
}
