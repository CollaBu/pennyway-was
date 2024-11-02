package kr.co.pennyway.api.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.common.response.ErrorResponse;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

public final class ApiTestHelper {
    private final TestRestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JwtProvider accessTokenProvider;

    public ApiTestHelper(TestRestTemplate restTemplate, ObjectMapper objectMapper, JwtProvider accessTokenProvider) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.accessTokenProvider = accessTokenProvider;
    }

    /**
     * API 요청을 보내고 응답을 처리하는 일반화된 메서드
     *
     * @param url                 API 엔드포인트 URL
     * @param method              HTTP 메서드
     * @param user                요청하는 사용자
     * @param request             요청 바디 (없을 경우 null)
     * @param successResponseType 성공 응답 타입
     * @param uriVariables        URL 변수들
     * @return ResponseEntity
     */
    public <T, R> ResponseEntity<?> callApi(
            String url,
            HttpMethod method,
            User user,
            T request,
            TypeReference<SuccessResponse<R>> successResponseType,
            Object... uriVariables) {

        ResponseEntity<Object> response = restTemplate.exchange(
                url,
                method,
                createHttpEntity(user, request),
                Object.class,
                uriVariables
        );

        Object body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("예상치 못한 반환 타입입니다. : " + response);
        }

        return response.getStatusCode().is2xxSuccessful()
                ? createSuccessResponse(response, body, successResponseType)
                : createErrorResponse(response, body);
    }

    /**
     * HTTP 요청 엔티티 생성
     */
    private <T> HttpEntity<?> createHttpEntity(User user, T request) {
        HttpHeaders headers = new HttpHeaders();
        if (user != null) {
            headers.set("Authorization", "Bearer " + generateToken(user));
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    /**
     * 사용자 토큰 생성
     */
    private String generateToken(User user) {
        return accessTokenProvider.generateToken(
                AccessTokenClaim.of(user.getId(), user.getRole().name())
        );
    }

    /**
     * 성공 응답 생성
     */
    private <R> ResponseEntity<SuccessResponse<R>> createSuccessResponse(
            ResponseEntity<Object> response,
            Object body,
            TypeReference<SuccessResponse<R>> successResponseType) {
        return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(objectMapper.convertValue(body, successResponseType));
    }

    /**
     * 에러 응답 생성
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(
            ResponseEntity<Object> response,
            Object body) {
        return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(objectMapper.convertValue(body, new TypeReference<ErrorResponse>() {
                }));
    }
}