package kr.co.pennyway.api.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.common.exception.PhoneVerificationErrorCode;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
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
    private final String expectedNonce = "testNonce";
    private final String expectedPhone = "010-1234-5678";
    private final String expectedCode = "123456";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OauthOidcHelper oauthOidcHelper;
    @SpyBean
    private PhoneCodeService phoneCodeService;
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

            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));
            userService.createUser(user);
            oauthService.createOauth(createOauthAccount(user, provider));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken, expectedNonce);

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

            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));
            userService.createUser(user);
            oauthService.createOauth(createOauthAccount(user, Provider.GOOGLE));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken, expectedNonce);

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

            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));
            userService.createUser(user);

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken, expectedNonce);

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

            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignIn(provider, expectedOauthId, expectedIdToken, expectedNonce);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.user.id").value(-1))
                    .andDo(print());
        }

        private ResultActions performOauthSignIn(Provider provider, String oauthId, String idToken, String nonce) throws Exception {
            SignInReq.Oauth request = new SignInReq.Oauth(oauthId, idToken, expectedNonce);

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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(Provider.KAKAO));

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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

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
        @DisplayName("같은 provider로 OAuth 회원가입 이력이 있으면 409 Conflict 에러가 발생한다.")
        void signUpWithSameProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, expectedCode);

            // then
            result
                    .andExpect(status().isConflict())
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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(Provider.KAKAO));

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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, "123457");

            // then
            result
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(PhoneVerificationErrorCode.IS_NOT_VALID_CODE.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(PhoneVerificationErrorCode.IS_NOT_VALID_CODE.getExplainError()))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("같은 provider로 Oauth 로그인 이력이 soft delete 되었으면 성공 응답을 반환한다.")
        void signUpWithDeletedOauth() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            oauthService.deleteOauth(oauth);
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

            // when
            ResultActions result = performOauthSignUpPhoneVerification(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("2000"))
                    .andExpect(jsonPath("$.data.sms.code").value(true))
                    .andExpect(jsonPath("$.data.sms.existsUser").value(true))
                    .andExpect(jsonPath("$.data.sms.username").value(user.getUsername()))
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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode, expectedOauthId);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").value(user.getId()))
                    .andDo(print());
            Oauth savedOauth = oauthService.readOauthByOauthIdAndProvider(expectedOauthId, provider).get();
            assertEquals(savedOauth.getUser().getId(), user.getId());
            System.out.println("oauth : " + savedOauth);
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
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode, expectedOauthId);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").value(user.getId()))
                    .andDo(print());
            Oauth savedOauth = oauthService.readOauthByOauthIdAndProvider(expectedOauthId, provider).get();
            assertEquals(savedOauth.getUser().getId(), user.getId());
            System.out.println("oauth : " + savedOauth);
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 회원가입 이력이 없으면, 동기화 요청 실패 후 400 에러가 발생한다.")
        void signUpWithNoSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode, expectedOauthId);

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
        @DisplayName("같은 provider로 OAuth 회원가입 이력이 있으면 409 Conflict 에러가 발생한다.")
        void signUpWithSameProvider() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode, expectedOauthId);

            // then
            result
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.ALREADY_SIGNUP_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("같은 provider로 Oauth 로그인 이력이 soft delete 되었으면, Oauth 정보가 복구되고 새로운 oauth_id를 반영한다.")
        void signUpWithDeletedOauth() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            oauthService.deleteOauth(oauth);
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, "newOauthId", expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", "newOauthId", "email"));

            // when
            ResultActions result = performOauthSignUpAccountLinking(provider, expectedCode, "newOauthId");

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").value(user.getId()))
                    .andDo(print());
            Oauth savedOauth = oauthService.readOauthByOauthIdAndProvider("newOauthId", provider).get();
            assertEquals(user.getId(), savedOauth.getUser().getId());
            assertEquals(oauth.getId(), savedOauth.getId());
            assertEquals("newOauthId", savedOauth.getOauthId());
            assertFalse(savedOauth.isDeleted());
            System.out.println("oauth : " + savedOauth);
        }

        private ResultActions performOauthSignUpAccountLinking(Provider provider, String code, String oauthId) throws Exception {
            SignUpReq.SyncWithAuth request = new SignUpReq.SyncWithAuth(oauthId, expectedIdToken, expectedNonce, expectedPhone, code);
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
        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 회원가입 이력이 없으면 새로운 회원가입이 성공하고 로그인 응답을 반환한다.")
        void signUpWithNoSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUp(provider, expectedCode);

            // then
            result
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.data.user.id").isNumber())
                    .andDo(print());
            Oauth savedOauth = oauthService.readOauthByOauthIdAndProvider(expectedOauthId, provider).get();
            assertNotNull(savedOauth.getUser().getId());
            System.out.println("oauth : " + savedOauth);
        }

        @Test
        @WithAnonymousUser
        @Transactional
        @DisplayName("기존의 일반 회원가입 이력이 있으면, 400 BAD_REQUEST 응답을 반환한다.")
        void signUpWithGeneralSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();

            userService.createUser(user);
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUp(provider, expectedCode);

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
        @DisplayName("OAuth로 가입한 유저가 이미 존재하면 400 에러가 발생한다.")
        void signUpWithOauthSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, Provider.GOOGLE);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            phoneCodeService.create(expectedPhone, expectedCode, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            given(oauthOidcHelper.getPayload(provider, expectedOauthId, expectedIdToken, expectedNonce)).willReturn(new OidcDecodePayload("iss", "aud", expectedOauthId, "email"));

            // when
            ResultActions result = performOauthSignUp(provider, expectedCode);

            // then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST.getExplainError()))
                    .andDo(print());
        }

        private ResultActions performOauthSignUp(Provider provider, String code) throws Exception {
            SignUpReq.Oauth request = new SignUpReq.Oauth(expectedOauthId, expectedIdToken, expectedNonce, "jayang", expectedUsername, expectedPhone, code);
            return mockMvc.perform(post("/v1/auth/oauth/sign-up")
                    .param("provider", provider.name())
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @Order(5)
    @DisplayName("[5] 소셜 회원가입 연동 해제")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OauthUnlinkTest {
        @Test
        @Order(1)
        @WithAnonymousUser
        @Transactional
        @DisplayName("제공자로 연동한 이력이 존재하지 않으면 404 에러가 발생한다.")
        void unlinkWithNoOauth() throws Exception {
            // given
            User user = createGeneralSignedUser();
            userService.createUser(user);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO);

            // then
            result
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.NOT_FOUND_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.NOT_FOUND_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @Order(2)
        @WithAnonymousUser
        @Transactional
        @DisplayName("제공자로 연동한 이력이 soft delete 되어 있으면 404 에러가 발생한다.")
        void unlinkWithSoftDeletedOauth() throws Exception {
            // given
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, Provider.KAKAO);

            userService.createUser(user);
            oauthService.createOauth(oauth);
            oauthService.deleteOauth(oauth);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO);

            // then
            result
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.NOT_FOUND_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.NOT_FOUND_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @Order(3)
        @WithAnonymousUser
        @Transactional
        @DisplayName("연동된 Oauth가 1개이고 일반 회원 이력이 없는 경우에는 409 에러가 발생한다.")
        void unlinkWithOnlyOauthSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createOauthSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);

            // when
            ResultActions result = performOauthUnlink(provider);

            // then
            result
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(OauthErrorCode.CANNOT_UNLINK_OAUTH.causedBy().getCode()))
                    .andExpect(jsonPath("$.message").value(OauthErrorCode.CANNOT_UNLINK_OAUTH.getExplainError()))
                    .andDo(print());
        }

        @Test
        @Order(4)
        @WithAnonymousUser
        @Transactional
        @DisplayName("연동된 Oauth가 1개이고 일반 회원 이력이 있는 경우에는 연동 해제에 성공한다.")
        void unlinkWithGeneralSignedUser() throws Exception {
            // given
            Provider provider = Provider.KAKAO;
            User user = createGeneralSignedUser();
            Oauth oauth = createOauthAccount(user, provider);

            userService.createUser(user);
            oauthService.createOauth(oauth);

            // when
            ResultActions result = performOauthUnlink(provider);

            // then
            result
                    .andExpect(status().isOk())
                    .andDo(print());
            assertFalse(oauthService.readOauthByOauthIdAndProvider(expectedOauthId, provider).isPresent());
        }

        @Test
        @Order(5)
        @WithAnonymousUser
        @Transactional
        @DisplayName("연동된 Oauth가 2개 이상이고 일반 회원 이력이 없는 경우에는 연동 해제에 성공한다.")
        void unlinkWithMultipleOauthSignedUser() throws Exception {
            // given
            User user = createOauthSignedUser();
            Oauth oauth1 = createOauthAccount(user, Provider.KAKAO);
            Oauth oauth2 = createOauthAccount(user, Provider.GOOGLE);

            userService.createUser(user);
            oauthService.createOauth(oauth1);
            oauthService.createOauth(oauth2);

            // when
            ResultActions result = performOauthUnlink(Provider.KAKAO);

            // then
            result
                    .andExpect(status().isOk())
                    .andDo(print());
            assertFalse(oauthService.readOauthByOauthIdAndProvider(expectedOauthId, Provider.KAKAO).isPresent());
        }

        private ResultActions performOauthUnlink(Provider provider) throws Exception {
            return mockMvc.perform(MockMvcRequestBuilders.delete("/v1/link-oauth")
                    .param("provider", provider.name()));
        }
    }
}
