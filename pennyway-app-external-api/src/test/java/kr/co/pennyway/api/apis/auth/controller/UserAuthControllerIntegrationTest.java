package kr.co.pennyway.api.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenProvider;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.api.config.supporter.WithSecurityMockUser;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshToken;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.oidc.OidcDecodePayload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UserAuthControllerIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccessTokenProvider accessTokenProvider;

    @Autowired
    private RefreshTokenProvider refreshTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private ForbiddenTokenService forbiddenTokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private OauthService oauthService;

    @MockBean
    private OauthOidcHelper oauthOidcHelper;

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("로그아웃")
    class SignOut {
        private String expectedAccessToken;
        private String expectedRefreshToken;
        private Long userId;

        @BeforeEach
        void setUp() {
            User user = User.builder()
                    .username("pennyway")
                    .password("password")
                    .profileVisibility(ProfileVisibility.PUBLIC)
                    .role(Role.USER)
                    .locked(Boolean.FALSE)
                    .build();
            userService.createUser(user);
            userId = user.getId();
            expectedAccessToken = accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), Role.USER.getType()));
            expectedRefreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(user.getId(), Role.USER.getType()));
        }

        @Order(1)
        @Test
        @DisplayName("Scenario #1 유효한 accessToken과 refreshToken이 있다면, accessToken은 forbiddenToken으로, refreshToken은 삭제한다.")
        void validAccessTokenAndValidRefreshToken() throws Exception {
            // given
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer " + expectedAccessToken)
                    .cookie(new Cookie("refreshToken", expectedRefreshToken)));

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));
            assertThrows(IllegalArgumentException.class, () -> refreshTokenService.delete(userId, expectedRefreshToken));
        }

        @Order(2)
        @Test
        @DisplayName("Scenario #2 유효한 accessToken만 존재한다면, accessToken만 forbiddenToken으로 만든다.")
        void validAccessTokenWithoutRefreshToken() throws Exception {
            // when
            ResultActions result = mockMvc.perform(performSignOut().header("Authorization", "Bearer " + expectedAccessToken));

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));
        }

        @Order(3)
        @Test
        @DisplayName("Scenario #2-1 유효한 accessToken과 다른 사용자의 유효한 refreshToken이 있다면, 401 에러를 반환한다. accessToken이 forbidden 처리되지 않으며, 사용자와 다른 사용자의 refreshToken 정보 모두 삭제되지 않는다.")
        void validAccessTokenAndWithOutOwnershipRefreshToken() throws Exception {
            // given
            String unexpectedRefreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(1000L, Role.USER.getType()));
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            refreshTokenService.save(RefreshToken.of(1000L, unexpectedRefreshToken, refreshTokenProvider.getExpiryDate(unexpectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc
                    .perform(performSignOut().header("Authorization", "Bearer " + expectedAccessToken)
                            .cookie(new Cookie("refreshToken", unexpectedRefreshToken)));

            // then
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(JwtErrorCode.WITHOUT_OWNERSHIP_REFRESH_TOKEN.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(JwtErrorCode.WITHOUT_OWNERSHIP_REFRESH_TOKEN.getExplainError()))
                    .andDo(print());
            assertDoesNotThrow(() -> refreshTokenService.delete(userId, expectedRefreshToken));
            assertDoesNotThrow(() -> refreshTokenService.delete(1000L, unexpectedRefreshToken));
            assertFalse(forbiddenTokenService.isForbidden(expectedAccessToken));
        }

        @Order(4)
        @Test
        @DisplayName("Scenario #2-2 유효한 accessToken과 유효하지 않은 refreshToken이 있다면, 401 에러를 반환한다. accessToken이 forbidden 처리되지 않으며, refreshToken 정보는 삭제되지 않는다.")
        void validAccessTokenAndInvalidRefreshToken() throws Exception {
            // given
            long ttl = refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, ttl));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer " + expectedAccessToken)
                    .cookie(new Cookie("refreshToken", "invalidRefreshToken")));

            // then
            result
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(JwtErrorCode.MALFORMED_TOKEN.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(JwtErrorCode.MALFORMED_TOKEN.getExplainError()))
                    .andDo(print());
            assertDoesNotThrow(() -> refreshTokenService.delete(userId, expectedRefreshToken));
            assertFalse(forbiddenTokenService.isForbidden(expectedAccessToken));
        }

        @Order(5)
        @Test
        @DisplayName("Scenario #2-3 유효한 accessToken, 유효한 refreshToken을 가진 사용자가 refresh 하기 전의 refreshToken을 사용하는 경우, accessToken을 forbidden에 등록하고 refreshToken을 cache에서 제거한다. (refreshToken 탈취 대체 시나리오)")
        void validAccessTokenAndOldRefreshToken() throws Exception {
            // given
            String oldRefreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(userId, Role.USER.getType()));
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer " + expectedAccessToken)
                    .cookie(new Cookie("refreshToken", oldRefreshToken)));

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andDo(print());
            assertThrows(IllegalArgumentException.class, () -> refreshTokenService.delete(userId, oldRefreshToken));
            assertThrows(IllegalArgumentException.class, () -> refreshTokenService.delete(userId, expectedRefreshToken));
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));
        }

        @Order(6)
        @Test
        @DisplayName("Scenario #3 유효하지 않은 accessToken과 유효한 refreshToken이 있다면 401 에러를 반환한다.")
        void invalidAccessTokenAndValidRefreshToken() throws Exception {
            // given
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer invalidToken")
                    .cookie(new Cookie("refreshToken", expectedRefreshToken)));

            // then
            result.andExpect(status().isUnauthorized()).andDo(print());
        }

        @Order(7)
        @Test
        @DisplayName("Scenario #4 유효하지 않은 accessToken과 유효하지 않은 refreshToken이 있다면 401 에러를 반환한다.")
        void invalidAccessTokenAndInvalidRefreshToken() throws Exception {
            // when
            ResultActions result = mockMvc.perform(performSignOut().header("Authorization", "Bearer invalidToken"));

            // then
            result.andExpect(status().isUnauthorized()).andDo(print());
        }

        private MockHttpServletRequestBuilder performSignOut() {
            return get("/v1/sign-out")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
        }
    }

    @Nested
    @Order(2)
    @DisplayName("소셜 계정 연동")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class LinkOauth {
        @Order(1)
        @Test
        @DisplayName("provider로 로그인한 이력이 없다면, 사용자는 계정 연동에 성공한다.")
        @WithSecurityMockUser(userId = "8")
        @Transactional
        void linkOauthWithNoHistory() throws Exception {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);
            Provider expectedProvider = Provider.KAKAO;
            given(oauthOidcHelper.getPayload(expectedProvider, "oauthId", "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", "oauthId", "email"));

            // when
            ResultActions result = performLinkOauth(expectedProvider, "oauthId");

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(oauthService.isExistOauthAccount(user.getId(), expectedProvider));
        }

        @Order(2)
        @Test
        @DisplayName("provider로 로그인한 이력이 있다면, 사용자는 계정 연동에 실패하고 409 에러를 반환한다.")
        @WithSecurityMockUser(userId = "9")
        @Transactional
        void linkOauthWithHistory() throws Exception {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);
            Provider expectedProvider = Provider.KAKAO;
            oauthService.createOauth(Oauth.of(expectedProvider, "oauthId", user));
            given(oauthOidcHelper.getPayload(expectedProvider, "oauthId", "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", "oauthId", "email"));

            // when
            ResultActions result = performLinkOauth(expectedProvider, "oauthId");

            // then
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Order(3)
        @Test
        @DisplayName("해당 provider가 soft delete된 이력이 존재한다면, deleted_at을 null로 업데이트하고 최신 oauth_id를 반영하여 계정 연동에 성공한다.")
        @WithSecurityMockUser(userId = "10")
        @Transactional
        void linkOauthWithDeletedHistory() throws Exception {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);
            Provider expectedProvider = Provider.KAKAO;
            Oauth oauth = Oauth.of(expectedProvider, "oauthId", user);
            oauthService.createOauth(oauth);
            oauthService.deleteOauth(oauth);
            given(oauthOidcHelper.getPayload(expectedProvider, "newOauthId", "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", "newOauthId", "email"));

            // when
            ResultActions result = performLinkOauth(expectedProvider, "newOauthId");

            // then
            result.andExpect(status().isOk()).andDo(print());
            Oauth savedOauth = oauthService.readOauth(oauth.getId()).orElse(null);
            assertNotNull(savedOauth);
            assertEquals("newOauthId", savedOauth.getOauthId());
            assertNull(savedOauth.getDeletedAt());
            log.info("연동된 Oauth 정보 : {}", savedOauth);
        }

        private ResultActions performLinkOauth(Provider provider, String oauthId) throws Exception {
            SignInReq.Oauth request = new SignInReq.Oauth(oauthId, "idToken", "nonce");
            return mockMvc.perform(put("/v1/link-oauth")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .queryParam("provider", provider.name())
                    .content(objectMapper.writeValueAsString(request)));
        }
    }
}
