package kr.co.pennyway.api.apis.ledger.integration;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.common.response.ErrorResponse;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@Slf4j
@ExternalApiIntegrationTest
public class SpendingCategoryUpdateIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private SpendingCustomCategoryService spendingCustomCategoryService;

    @Autowired
    private JwtProvider accessTokenProvider;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("지출 카테고리에 접근 권한이 없는 경우 403 Forbidden 응답을 받는다.")
    void withOutPermission() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

        // when
        ResponseEntity<?> response = failureRequest(user, user.getId(), "name", SpendingCategory.FOOD.name());

        // then
        assertEquals("403 Forbidden 예외가 발생해야 합니다.", response.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<SuccessResponse<SpendingCategoryDto.Res>> successRequest(User user, Long spendingCategoryId, String name, String icon) {
        return restTemplate.exchange(
                createUriComponentsBuilder(spendingCategoryId, name, icon).toUriString(),
                org.springframework.http.HttpMethod.PATCH,
                createHttpEntity(user),
                new ParameterizedTypeReference<SuccessResponse<SpendingCategoryDto.Res>>() {
                }
        );
    }

    private ResponseEntity<ErrorResponse> failureRequest(User user, Long spendingCategoryId, String name, String icon) {
        return restTemplate.exchange(
                createUriComponentsBuilder(spendingCategoryId, name, icon).toUriString(),
                org.springframework.http.HttpMethod.PATCH,
                createHttpEntity(user),
                ErrorResponse.class
        );
    }

    private UriComponentsBuilder createUriComponentsBuilder(Long spendingCategoryId, String name, String icon) {
        // 기본 URL 설정
        String baseUrl = "http://localhost:" + port + "/v2/spending-categories/" + spendingCategoryId;

        // 쿼리 파라미터 설정
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("name", name)
                .queryParam("icon", icon);
    }

    private HttpEntity<?> createHttpEntity(User user) {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), user.getRole().name())));

        // 요청 Entity 생성 (empty body)
        return new HttpEntity<>(headers);
    }
}
