package kr.co.pennyway.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 발생 이유 코드
 */
@Getter
@RequiredArgsConstructor
public enum ReasonCode {
    /* 400_BAD_REQUEST */
    INVALID_REQUEST_SYNTAX(0),
    MISSING_REQUIRED_PARAMETER(1),
    MALFORMED_PARAMETER(2),
    MALFORMED_REQUEST_BODY(3),
    INVALID_REQUEST(4),

    /* 401_UNAUTHORIZED */
    MISSING_OR_INVALID_AUTHENTICATION_CREDENTIALS(0),
    EXPIRED_OR_REVOKED_TOKEN(1),
    INSUFFICIENT_PERMISSIONS(2),
    TAMPERED_OR_MALFORMED_TOKEN(3),
    WITHOUT_OWNERSHIP(4),

    /* 403_FORBIDDEN */
    ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN(0),
    IP_ADDRESS_BLOCKED(1),
    USER_ACCOUNT_SUSPENDED_OR_BANNED(2),
    ACCESS_TO_RESOURCE_NOT_ALLOWED_FOR_USER_ROLE(3),

    /* 404_NOT_FOUND */
    REQUESTED_RESOURCE_NOT_FOUND(0),
    INVALID_URL_OR_ENDPOINT(1),
    RESOURCE_DELETED_OR_MOVED(2),

    /* 405_METHOD_NOT_ALLOWED */
    REQUEST_METHOD_NOT_SUPPORTED(0),
    ATTEMPTED_TO_ACCESS_UNSUPPORTED_METHOD(1),

    /* 406_NOT_ACCEPTABLE */
    REQUESTED_RESPONSE_FORMAT_NOT_SUPPORTED(0),

    /* 409_CONFLICT */
    REQUEST_CONFLICTS_WITH_CURRENT_STATE_OF_RESOURCE(0),
    RESOURCE_ALREADY_EXISTS(1),
    CONCURRENT_MODIFICATION_CONFLICT(2),

    /* 412_PRECONDITION_FAILED */
    PRECONDITION_REQUEST_HEADER_NOT_MATCHED(0),
    IF_MATCH_HEADER_OR_IF_NONE_MATCH_HEADER_NOT_MATCHED(1),

    /* 422_UNPROCESSABLE_CONTENT */
    REQUIRED_PARAMETERS_MISSING_IN_REQUEST_BODY(0),
    VALIDATION_ERROR_IN_REQUEST_BODY(1),
    TYPE_MISMATCH_ERROR_IN_REQUEST_BODY(2),

    /* 500_INTERNAL_SERVER_ERROR */
    UNEXPECTED_ERROR(0),
    ;

    private final int code;

    @Override
    public String toString() {
        return name();
    }
}
