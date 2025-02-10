package kr.co.pennyway.socket.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public record ServerSideMessage(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String code,
        String reason
) {
    public ServerSideMessage {
        Objects.requireNonNull(reason, "reason must not be null");
    }

    public static ServerSideMessage of(String reason) {
        return new ServerSideMessage(null, reason);
    }

    public static ServerSideMessage of(String code, String reason) {
        return new ServerSideMessage(code, reason);
    }
}