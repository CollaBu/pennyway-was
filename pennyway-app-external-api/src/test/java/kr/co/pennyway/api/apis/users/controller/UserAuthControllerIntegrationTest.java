package kr.co.pennyway.api.apis.users.controller;

import jakarta.servlet.http.Cookie;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenProvider;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshToken;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UserAuthControllerIntegrationTest extends ExternalApiDBTestConfig {
    @Autowired
    private MockMvc mockMvc;

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
        @WithMockUser
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
        @WithMockUser
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
        @WithMockUser
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
        @WithMockUser
        @DisplayName("Scenario #2-2 유효한 accessToken과 유효하지 않은 refreshToken이 있다면, 401 에러를 반환한다. accessToken이 forbidden 처리되지 않으며, refreshToken 정보는 삭제되지 않는다.")
        void validAccessTokenAndInvalidRefreshToken() throws Exception {
            // given
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer " + expectedAccessToken)
                    .cookie(new Cookie("refreshToken", "invalidToken")));

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
        @WithMockUser
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

        @Order(6)
        @Test
        @WithMockUser
        @DisplayName("Scenario #4 유효하지 않은 accessToken과 유효하지 않은 refreshToken이 있다면 401 에러를 반환한다.")
        void invalidAccessTokenAndInvalidRefreshToken() throws Exception {
            // when
            ResultActions result = mockMvc.perform(performSignOut().header("Authorization", "Bearer invalidToken"));

            // then
            result.andExpect(status().isUnauthorized()).andDo(print());
        }

        private MockHttpServletRequestBuilder performSignOut() {
            return get("/v1/users/sign-out")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
        }
    }
}
