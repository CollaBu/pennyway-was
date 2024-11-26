package kr.co.pennyway.api.apis.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenProvider;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.ForbiddenTokenService;
import kr.co.pennyway.domain.context.account.service.OauthService;
import kr.co.pennyway.domain.context.account.service.RefreshTokenService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.refresh.domain.RefreshToken;
import kr.co.pennyway.domain.domains.user.domain.User;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    @DisplayName("로그아웃")
    class SignOut {
        private String expectedAccessToken;
        private String expectedRefreshToken;
        private String expectedDeviceId;
        private Long userId;

        @BeforeEach
        void setUp() {
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);
            userId = user.getId();
            expectedDeviceId = "AA-BBB-CC-DDD";
            expectedAccessToken = accessTokenProvider.generateToken(AccessTokenClaim.of(user.getId(), Role.USER.getType()));
            expectedRefreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(user.getId(), expectedDeviceId, Role.USER.getType()));
        }

        @Test
        @DisplayName("Scenario #1 유효한 accessToken과 refreshToken이 있다면, accessToken은 forbiddenToken으로, refreshToken은 삭제한다.")
        void validAccessTokenAndValidRefreshToken() throws Exception {
            // given
            refreshTokenService.create(RefreshToken.of(userId, expectedDeviceId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer " + expectedAccessToken)
                    .cookie(new Cookie("refreshToken", expectedRefreshToken)));

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));
            refreshTokenService.deleteAll(userId);
        }

        @Test
        @DisplayName("Scenario #2 유효한 accessToken만 존재한다면, accessToken만 forbiddenToken으로 만든다.")
        void validAccessTokenWithoutRefreshToken() throws Exception {
            // when
            ResultActions result = mockMvc.perform(performSignOut().header("Authorization", "Bearer " + expectedAccessToken));

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));
        }

        @Test
        @DisplayName("Scenario #2-1 유효한 accessToken과 다른 사용자의 유효한 refreshToken이 있다면, 401 에러를 반환한다. accessToken이 forbidden 처리되지 않으며, 사용자와 다른 사용자의 refreshToken 정보 모두 삭제되지 않는다.")
        void validAccessTokenAndWithOutOwnershipRefreshToken() throws Exception {
            // given
            String otherDeviceId = "BB-CCC-DDD";
            String unexpectedRefreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(1000L, otherDeviceId, Role.USER.getType()));
            refreshTokenService.create(RefreshToken.of(userId, expectedDeviceId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            refreshTokenService.create(RefreshToken.of(1000L, otherDeviceId, unexpectedRefreshToken, refreshTokenProvider.getExpiryDate(unexpectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc
                    .perform(performSignOut().header("Authorization", "Bearer " + expectedAccessToken)
                            .cookie(new Cookie("refreshToken", unexpectedRefreshToken)));

            // then
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(JwtErrorCode.WITHOUT_OWNERSHIP_REFRESH_TOKEN.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(JwtErrorCode.WITHOUT_OWNERSHIP_REFRESH_TOKEN.getExplainError()))
                    .andDo(print());
            assertFalse(forbiddenTokenService.isForbidden(expectedAccessToken));

            refreshTokenService.deleteAll(userId);
            refreshTokenService.deleteAll(1000L);
        }

        @Test
        @DisplayName("Scenario #2-2 유효한 accessToken과 유효하지 않은 refreshToken이 있다면, 401 에러를 반환한다. accessToken이 forbidden 처리되지 않으며, refreshToken 정보는 삭제되지 않는다.")
        void validAccessTokenAndInvalidRefreshToken() throws Exception {
            // given
            long ttl = refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            refreshTokenService.create(RefreshToken.of(userId, expectedDeviceId, expectedRefreshToken, ttl));

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
            assertFalse(forbiddenTokenService.isForbidden(expectedAccessToken));

            refreshTokenService.deleteAll(userId);
        }

        @Test
        @DisplayName("Scenario #2-3 유효한 accessToken, 유효한 refreshToken을 가진 사용자가 refresh 하기 전의 refreshToken을 사용하는 경우, accessToken을 forbidden에 등록하고 refreshToken을 cache에서 제거한다. (refreshToken 탈취 대체 시나리오)")
        void validAccessTokenAndOldRefreshToken() throws Exception {
            // given
            String oldRefreshToken = refreshTokenProvider.generateToken(RefreshTokenClaim.of(userId, expectedDeviceId, Role.USER.getType()));
            refreshTokenService.create(RefreshToken.of(userId, expectedDeviceId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer " + expectedAccessToken)
                    .cookie(new Cookie("refreshToken", oldRefreshToken)));

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andDo(print());
            assertTrue(forbiddenTokenService.isForbidden(expectedAccessToken));

            refreshTokenService.deleteAll(userId);
        }

        @Test
        @DisplayName("Scenario #3 유효하지 않은 accessToken과 유효한 refreshToken이 있다면 401 에러를 반환한다.")
        void invalidAccessTokenAndValidRefreshToken() throws Exception {
            // given
            refreshTokenService.create(RefreshToken.of(userId, expectedDeviceId, expectedRefreshToken, refreshTokenProvider.getExpiryDate(expectedRefreshToken).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            // when
            ResultActions result = mockMvc.perform(performSignOut()
                    .header("Authorization", "Bearer invalidToken")
                    .cookie(new Cookie("refreshToken", expectedRefreshToken)));

            // then
            result.andExpect(status().isUnauthorized()).andDo(print());
        }

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
    @DisplayName("소셜 계정 연동")
    class LinkOauth {
        @Test
        @DisplayName("provider로 로그인한 이력이 없다면, 사용자는 계정 연동에 성공한다.")
        @Transactional
        void linkOauthWithNoHistory() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            Provider expectedProvider = Provider.KAKAO;
            given(oauthOidcHelper.getPayload(expectedProvider, "oauthId", "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", "oauthId", "email"));

            // when
            ResultActions result = performLinkOauth(expectedProvider, "oauthId", user);

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertTrue(oauthService.isExistOauthByUserIdAndProvider(user.getId(), expectedProvider));
        }

        @Test
        @DisplayName("이미 해당 소셜 계정에 연동했다면, ALREADY_SIGNUP_OAUTH 에러를 반환한다.")
        @Transactional
        void linkOauthWithHistory() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            Provider expectedProvider = Provider.KAKAO;
            oauthService.createOauth(Oauth.of(expectedProvider, "oauthId", user));
            given(oauthOidcHelper.getPayload(expectedProvider, "oauthId", "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", "oauthId", "email"));

            // when
            ResultActions result = performLinkOauth(expectedProvider, "oauthId", user);

            // then
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @DisplayName("해당 소셜 계정으로 연동했었던 이력이 삭제되어 있다면, 새로운 데이터를 생성하고 연동에 성공한다.")
        @Transactional
        void linkOauthWithDeletedHistory() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            Provider expectedProvider = Provider.KAKAO;
            Oauth oauth = oauthService.createOauth(Oauth.of(expectedProvider, "oauthId", user));
            oauthService.deleteOauth(oauth);

            given(oauthOidcHelper.getPayload(expectedProvider, "newOauthId", "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", "newOauthId", "email"));

            // when
            ResultActions result = performLinkOauth(expectedProvider, "newOauthId", user);

            // then
            result.andExpect(status().isOk()).andDo(print());
        }

        @Test
        @DisplayName("다른 계정에 이미 해당 소셜 계정이 연동되어 있다면, 409 ALREADY_USED_OAUTH 에러를 반환한다.")
        @Transactional
        void linkOauthWithAlreadyUsedOauth() throws Exception {
            // given
            User user1 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Provider provider = Provider.KAKAO;
            String oauthId = "oauthId";
            oauthService.createOauth(Oauth.of(provider, oauthId, user1));

            User user2 = userService.createUser(UserFixture.GENERAL_USER.toUser());

            given(oauthOidcHelper.getPayload(provider, oauthId, "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", oauthId, "email"));

            // when
            ResultActions result = performLinkOauth(provider, oauthId, user2);

            // then
            result
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.ALREADY_USED_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.ALREADY_USED_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @DisplayName("다른 계정에서 해당 소셜 계정을 연동했었던 삭제 이력이 있다면, 새로운 데이터를 생성하고 연동에 성공한다.")
        void linkOauthWithDeletedOauth() throws Exception {
            // given
            User user1 = userService.createUser(UserFixture.GENERAL_USER.toUser());
            Provider provider = Provider.KAKAO;
            String oauthId = "oauthId";
            Oauth oauth = oauthService.createOauth(Oauth.of(provider, oauthId, user1));
            log.info("생성된 Oauth 정보 : {}", oauth);
            oauthService.deleteOauth(oauth);

            User user2 = userService.createUser(UserFixture.OAUTH_USER.toUser());

            given(oauthOidcHelper.getPayload(provider, oauthId, "idToken", "nonce")).willReturn(new OidcDecodePayload("iss", "aud", oauthId, "email"));

            // when
            ResultActions result = performLinkOauth(provider, oauthId, user2);

            // then
            result.andExpect(status().isOk()).andDo(print());
            Oauth savedOauth = oauthService.readOauthsByUserId(user2.getId()).stream().filter(o -> o.getProvider().equals(provider)).findFirst().orElse(null);
            assertNotNull(savedOauth);
            assertNull(savedOauth.getDeletedAt());
        }

        private ResultActions performLinkOauth(Provider provider, String oauthId, User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);
            SignInReq.Oauth request = new SignInReq.Oauth(oauthId, "idToken", "nonce", "deviceId");

            return mockMvc.perform(put("/v1/link-oauth")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(user(userDetails))
                    .queryParam("provider", provider.name())
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(5)
    @DisplayName("소셜 연동 해제")
    class OauthUnlinkTest {
        @Test
        @Transactional
        @DisplayName("제공자로 연동한 이력이 존재하지 않으면 404 에러가 발생한다.")
        void unlinkWithNoOauth() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO, user);

            // then
            result
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.NOT_FOUND_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.NOT_FOUND_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @Transactional
        @DisplayName("제공자로 연동한 이력이 soft delete 되어 있으면 404 에러가 발생한다.")
        void unlinkWithSoftDeletedOauth() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            Oauth oauth = mappingOauthWithUser(user, Provider.KAKAO);
            oauthService.deleteOauth(oauth);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO, user);

            // then
            result
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.NOT_FOUND_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.NOT_FOUND_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @Transactional
        @DisplayName("연동된 Oauth가 1개이고 일반 회원 이력이 없는 경우에는 409 에러가 발생한다.")
        void unlinkWithOnlyOauthSignedUser() throws Exception {
            // given
            User user = userService.createUser(UserFixture.OAUTH_USER.toUser());

            mappingOauthWithUser(user, Provider.KAKAO);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO, user);

            // then
            result
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.CANNOT_UNLINK_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.CANNOT_UNLINK_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @Transactional
        @DisplayName("연동된 Oauth가 1개이고 일반 회원 이력이 있는 경우에는 연동 해제에 성공한다.")
        void unlinkWithGeneralSignedUser() throws Exception {
            // given
            User user = userService.createUser(UserFixture.GENERAL_USER.toUser());

            Oauth oauth = mappingOauthWithUser(user, Provider.KAKAO);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO, user);

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertNull(oauthService.readOauthByOauthIdAndProvider(oauth.getOauthId(), Provider.KAKAO).orElse(null));
        }

        @Test
        @Transactional
        @DisplayName("연동된 Oauth가 2개 이상이고 일반 회원 이력이 없는 경우에는 연동 해제에 성공한다.")
        void unlinkWithMultipleOauthSignedUser() throws Exception {
            // given
            User user = userService.createUser(UserFixture.OAUTH_USER.toUser());

            Oauth kakao = mappingOauthWithUser(user, Provider.KAKAO);
            Oauth google = mappingOauthWithUser(user, Provider.GOOGLE);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO, user);

            // then
            result.andExpect(status().isOk()).andDo(print());
            assertNull(oauthService.readOauthByOauthIdAndProvider(kakao.getOauthId(), Provider.KAKAO).orElse(null));
        }

        private ResultActions performOauthUnlink(Provider provider, User requestUser) throws Exception {
            UserDetails userDetails = SecurityUserDetails.from(requestUser);

            return mockMvc.perform(MockMvcRequestBuilders.delete("/v1/link-oauth")
                    .with(user(userDetails))
                    .param("provider", provider.name()));
        }

        private Oauth mappingOauthWithUser(User user, Provider provider) {
            Oauth oauth = Oauth.of(provider, "oauthId", user);
            return oauthService.createOauth(oauth);
        }
    }
}
