package kr.co.pennyway.api.common.util;

import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public class RequestParameters {
    private final String url;
    private final HttpMethod method;
    private final User user;
    private final Object request;
    private final Map<String, String> queryParams;
    private final Object[] uriVariables;

    private RequestParameters(String url, HttpMethod method, User user,
                              Object request, Map<String, String> queryParams, Object[] uriVariables) {
        validateRequired(url, "URL must not be null");
        validateRequired(method, "HTTP method must not be null");
        validateRequired(user, "User must not be null");

        this.url = url;
        this.method = method;
        this.user = user;
        this.request = request;
        this.queryParams = queryParams;
        this.uriVariables = uriVariables;
    }

    /**
     * RequestParameters 생성을 위한 편의 메서드
     * GET 메서드를 사용하는 경우에 사용합니다.
     */
    public static RequestParametersBuilder defaultGet(String url) {
        return RequestParameters.builder()
                .url(url)
                .method(HttpMethod.GET);
    }

    /**
     * RequestParameters 생성을 위한 편의 메서드
     * POST 메서드를 사용하는 경우에 사용합니다.
     */
    public static RequestParametersBuilder defaultPost(String url) {
        return RequestParameters.builder()
                .url(url)
                .method(HttpMethod.POST);
    }

    /**
     * RequestParameters 생성을 위한 편의 메서드
     * PUT 메서드를 사용하는 경우에 사용합니다.
     */
    public static RequestParametersBuilder defaultPut(String url) {
        return RequestParameters.builder()
                .url(url)
                .method(HttpMethod.PUT);
    }

    /**
     * RequestParameters 생성을 위한 편의 메서드
     * DELETE 메서드를 사용하는 경우에 사용합니다.
     */
    public static RequestParametersBuilder defaultDelete(String url) {
        return RequestParameters.builder()
                .url(url)
                .method(HttpMethod.DELETE);
    }

    /**
     * 쿼리 파라미터 추가를 위한 편의 메서드
     * key-value 쌍으로 쿼리 파라미터를 생성합니다.
     *
     * @throws IllegalArgumentException key-value 쌍이 제대로 제공되지 않은 경우
     */
    public static Map<String, String> createQueryParams(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be provided");
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            params.put(String.valueOf(keyValues[i]), String.valueOf(keyValues[i + 1]));
        }
        return params;
    }

    private void validateRequired(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * URI를 생성합니다.
     * 쿼리 파라미터와 URI 변수를 적용합니다.
     */
    public URI createUri() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        // 쿼리 파라미터 적용
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(builder::queryParam);
        }

        // URI 변수 적용
        return (uriVariables != null && uriVariables.length > 0)
                ? builder.buildAndExpand(uriVariables).toUri()
                : builder.build().toUri();
    }
}
