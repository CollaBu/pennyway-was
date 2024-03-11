package kr.co.pennyway.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * HTTP 상태 코드
 */
@Getter
@RequiredArgsConstructor
public enum StatusCode {
    SUCCESS(200),
    CREATED(201),
    NO_CONTENT(204),

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),

    CONFLICT(409),
    PRECONDITION_FAILED(412),
    UNPROCESSABLE_CONTENT(422),
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503);

    private final int code;
}