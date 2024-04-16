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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).toEpochSecond(ZoneOffset.UTC)));

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
        void validAccessTokenAndInvalidRefreshToken() throws Exception {
            // when
            ResultActions result = mockMvc.perform(performSignOut().header("Authorization", "Bearer " + expectedAccessToken));

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));
        }

        @Order(3)
        @Test
        @WithMockUser
        @DisplayName("Scenario #3 유효하지 않은 accessToken과 유효한 refreshToken이 있다면 401 에러를 반환한다.")
        void invalidAccessTokenAndValidRefreshToken() throws Exception {
            // given
            refreshTokenService.save(RefreshToken.of(userId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).toEpochSecond(ZoneOffset.UTC)));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer invalidToken")
                    .cookie(new Cookie("refreshToken", expectedRefreshToken)));

            // then
            result.andExpect(status().isUnauthorized()).andDo(print());
        }

        @Order(4)
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
