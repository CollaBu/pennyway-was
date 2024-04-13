package kr.co.pennyway.api.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
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
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OauthOidcHelper oauthOidcHelper;
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
                    .andExpect(jsonPath("$.data.user.id").value(1))
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

    }

    @Nested
    @Order(3)
    @DisplayName("[4-1] 소셜 회원가입 계정 연동")
    class OauthSignUpAccountLinkingTest {

    }

    @Nested
    @Order(4)
    @DisplayName("[4-2] 소셜 회원가입")
    class OauthSignUpTest {

    }
}
