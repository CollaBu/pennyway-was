package kr.co.pennyway.api.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.common.exception.PhoneVerificationErrorCode;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import kr.co.pennyway.infra.common.oidc.OidcDecodePayload;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExternalApiIntegrationTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "oauth2.client.provider.kakao.jwks-uri=http://localhost:${wiremock.server.port}")
public class OAuthControllerIntegrationTest extends ExternalApiDBTestConfig {
    private final String expectedUsername = "jayang";

    private final String expectedOauthId = "testOauthId";
    private final String expectedIdToken = "testIdToken";
    private final String expectedPhone = "010-1234-5678";
    private final String expectedCode = "123456";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OauthOidcHelper oauthOidcHelper;
    @SpyBean
    private PhoneVerificationService phoneVerificationService;
    @Autowired
    private UserService userService;
    @Autowired
    private OauthService oauthService;

    /**
     * 일반 회원가입 유저 생성
     */
    private User createGeneralSignedUser() {
        return User.builder()
                .name("페니웨이")
                .username(expectedUsername)
                .password("dkssudgktpdy1")
                .phone("010-1234-5678")
                .role(Role.USER)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .build();
    }

    /**
     * OAuth로 가입한 유저 생성 (password가 NULL)
     */
    private User createOauthSignedUser() {
        return User.builder()
                .name("페니웨이")
                .username(expectedUsername)
                .phone("010-1234-5678")
                .role(Role.USER)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .build();
    }

    /**
     * User에 연결된 Oauth 생성
     */
    private Oauth createOauthAccount(User user, Provider provider) {
        return Oauth.of(provider, expectedOauthId, user);
    }

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) throws IOException {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/**").with(csrf()))
                .build();
        Path path = ResourceUtils.getFile("classpath:payload/oidc-response.json").toPath();
        stubFor(
                get(urlPathEqualTo("/.well-known/jwks.json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(Files.readAllBytes(path))
                        )
        );
    }

    @Nested
    @Order(1)
    @DisplayName("[1] 소셜 로그인")
    class OauthSignInTest {
        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("provider로 로그인한 소셜 계정이 있으면 로그인에 성공한다.")
        void signInWithOauth() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();

            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));
            userService.createUser(user);
            oauthService.createOauth(createOauthAccount(user, provider));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").value(user.getId()))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("다른 provider로 로그인한 소셜 계정이 있으면 user id가 -1로 반환된다.")
        void signInWithDifferentProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();

            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));
            userService.createUser(user);
            oauthService.createOauth(createOauthAccount(user, Provider.GOOGLE));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.user.id").value(-1))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("일반 회원가입 이력만 존재하는 경우에는 user id가 -1로 반환된다.")
        void signInWithGeneralSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();

            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));
            userService.createUser(user);

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.user.id").value(-1))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("회원 가입 이력이 없는 사용자의 경우에 user id가 -1로 반환된다.")
        void signInWithNoSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;

            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.user.id").value(-1))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("OAuth id와 payload의 sub가 다른 경우에는 NOT_MATCHED_OAUTH_ID 에러가 발생한다.")
        void signInWithNotMatchedOauthId() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();

            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", "differentOauthId", "email"));
            userService.createUser(user);
            oauthService.createOauth(createOauthAccount(user, provider));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken);

            // then
            result
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.NOT_MATCHED_OAUTH_ID.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.NOT_MATCHED_OAUTH_ID.getExplainError()))
                    .andDo(print());
        }

        private ResultActions performOauthSignIn(Provider provider, String oauthId, String idToken) throws Exception {
            SignInReq.Oauth request = new SignInReq.Oauth(oauthId, idToken);

            return mockMvc.perform(post("/v1/auth/oauth/sign-in")
                    .param("provider", provider.name())
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(2)
    @DisplayName("[3] 소셜 회원가입 전화번호 인증")
    class OauthSignUpPhoneVerificationTest {
        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 일반 회원가입 이력이 있으면, existsUser가 true고 username이 반환된다.")
        void signUpWithGeneralSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();

            userService.createUser(user);
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andExpect(jsonPath("$.data.sms.code").value(true))
                    .andExpect(jsonPath("$.data.sms.existsUser").value(true))
                    .andExpect(jsonPath("$.data.sms.username").value(expectedUsername))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 다른 provider OAuth 회원가입 이력이 있으면, existsUser가 true고 username이 반환된다.")
        void signUpWithDifferentProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, Provider.GOOGLE);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(Provider.KAKAO));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andExpect(jsonPath("$.data.sms.code").value(true))
                    .andExpect(jsonPath("$.data.sms.existsUser").value(true))
                    .andExpect(jsonPath("$.data.sms.username").value(expectedUsername))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 회원가입 이력이 없으면 existsUser가 false고 username 필드가 없다.")
        void signUpWithNoSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andExpect(jsonPath("$.data.sms.code").value(true))
                    .andExpect(jsonPath("$.data.sms.existsUser").value(false))
                    .andExpect(jsonPath("$.data.sms.username").doesNotExist())
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("같은 provider로 OAuth 회원가입 이력이 있으면 400 에러가 발생한다.")
        void signUpWithSameProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, expectedCode);

            // then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("인증 코드를 요청한 provider와 다른 provider로 인증 코드를 입력하면 404 에러가 발생한다.")
        void signUpWithDifferentProviderCode() throws Exception {
            // given
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(Provider.KAKAO));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(Provider.GOOGLE, expectedCode);

            // then
            result
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(PhoneVerificationErrorCode.EXPIRED_OR_INVALID_PHONE.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(PhoneVerificationErrorCode.EXPIRED_OR_INVALID_PHONE.getExplainError()))
                    .andDo(print());
        }


        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("인증 코드가 틀리면 401 에러가 발생한다.")
        void signUpWithInvalidCode() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, "123457");

            // then
            result
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(PhoneVerificationErrorCode.IS_NOT_VALID_CODE.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(PhoneVerificationErrorCode.IS_NOT_VALID_CODE.getExplainError()))
                    .andDo(print());
        }

        private ResultActions performOauthSignUpPhoneVerification(Provider provider, String code) throws Exception {
            PhoneVerificationDto.VerifyCodeReq request = new PhoneVerificationDto.VerifyCodeReq(expectedPhone, code);
            return mockMvc.perform(post("/v1/auth/oauth/phone/verification")
                    .param("provider", provider.name())
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("[4-1] 소셜 회원가입 계정 연동")
    class OauthSignUpAccountLinkingTest {
        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 일반 회원가입 이력이 있으면, 해당 user entity에 OAuth 정보가 추가되고 로그인에 성공한다.")
        void signUpWithGeneralSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();

            userService.createUser(user);
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").value(user.getId()))
                    .andDo(print());
            assertEquals(oauthService.readOauthByOauthIdAndProvider(expectedOauthId, provider).get().getUser().getId(), user.getId());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 다른 provider OAuth 회원가입 이력이 있으면, user entity에 OAuth 정보가 추가되고 로그인에 성공한다.")
        void signUpWithDifferentProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, Provider.GOOGLE);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").value(user.getId()))
                    .andDo(print());
            assertEquals(oauthService.readOauthByOauthIdAndProvider(expectedOauthId, provider).get().getUser().getId(), user.getId());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 회원가입 이력이 없으면, 동기화 요청 실패 후 400 에러가 발생한다.")
        void signUpWithNoSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode);

            // then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST.getExplainError()))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("같은 provider로 OAuth 회원가입 이력이 있으면 400 에러가 발생한다.")
        void signUpWithSameProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneVerificationService.create(expectedPhone, expectedCode, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedIdToken)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode);

            // then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.getExplainError()))
                    .andDo(print());
        }

        private ResultActions performOauthSignUpAccountLinking(Provider provider, String code) throws Exception {
            SignUpReq.SyncWithAuth request = new SignUpReq.SyncWithAuth(expectedIdToken, expectedPhone, code);
            return mockMvc.perform(post("/v1/auth/oauth/link-auth")
                    .param("provider", provider.name())
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("[4-2] 소셜 회원가입")
    class OauthSignUpTest {

    }
}