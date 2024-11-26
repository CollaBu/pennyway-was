package kr.co.pennyway.api.apis.ledger.integration;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.common.response.ErrorResponse;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.SpendingCustomCategoryFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@Slf4j
@ExternalApiIntegrationTest
public class SpendingCategoryUpdateIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private SpendingCategoryService spendingCategoryService;

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

    @Test
    @DisplayName("지출 카테고리가 수정되면, 수정된 정보를 응답 받는다.")
    void success() {
        // given
        User user = userService.createUser(UserFixture.GENERAL_USER.toUser());
        SpendingCustomCategory category = spendingCategoryService.createSpendingCustomCategory(SpendingCustomCategoryFixture.GENERAL_SPENDING_CUSTOM_CATEGORY.toCustomSpendingCategory(user));
        String expectedName = "뉴 카테고리";

        // when
        ResponseEntity<SuccessResponse<Map<String, SpendingCategoryDto.Res>>> response = successRequest(user, category.getId(), expectedName, SpendingCategory.HEALTH.name());
        SpendingCategoryDto.Res data = response.getBody().getData().get("spendingCategory");

        // then
        assertEquals("200 OK 응답을 받아야 합니다.", response.getStatusCode(), HttpStatus.OK);
        assertEquals("수정된 지출 카테고리 이름이 일치해야 합니다.", expectedName, data.name());
        assertEquals("수정된 지출 카테고리 아이콘이 일치해야 합니다.", SpendingCategory.HEALTH.name(), data.icon().name());
    }

    private ResponseEntity<SuccessResponse<Map<String, SpendingCategoryDto.Res>>> successRequest(User user, Long spendingCategoryId, String name, String icon) {
        return restTemplate.exchange(
                createUriComponentsBuilder(spendingCategoryId, name, icon),
                HttpMethod.PATCH,
                createHttpEntity(user),
                new ParameterizedTypeReference<SuccessResponse<Map<String, SpendingCategoryDto.Res>>>() {
                }
        );
    }

    private ResponseEntity<ErrorResponse> failureRequest(User user, Long spendingCategoryId, String name, String icon) {
        return restTemplate.exchange(
                createUriComponentsBuilder(spendingCategoryId, name, icon),
                HttpMethod.PATCH,
                createHttpEntity(user),
                ErrorResponse.class
        );
    }

    private String createUriComponentsBuilder(Long spendingCategoryId, String name, String icon) {
        // 기본 URL 설정
        String baseUrl = "http://localhost:" + port + "/v2/spending-categories/" + spendingCategoryId.toString();

        // 쿼리 파라미터 설정
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("name", name)
                .queryParam("icon", icon)
                .build(false).toUriString(); // encoded false 옵션으로 한글 깨짐 방지
    }

    private HttpEntity<?> createHttpEntity(User user) {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), user.getRole().name())));

        // 요청 Entity 생성 (empty body)
        return new HttpEntity<>(headers);
    }
}
