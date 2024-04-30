package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.AuthStateDto;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.apis.auth.service.UserOauthSignService;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserAuthUseCaseUnitTest {
    private final String secretStr = "helloMyNameIsPennywayThisIsSecretKeyItNeedsToBeLongerThan256Bits";
    private final JwtClaims jwtClaims = AccessTokenClaim.of(1L, "ROLE_USER");
    private JwtProvider accessTokenProvider;
    private UserAuthUseCase userAuthUseCase;
    private JwtAuthHelper jwtAuthHelper;

    @Mock
    private UserOauthSignService userOauthSignService;
    @Mock
    private OauthOidcHelper oauthOidcHelper;
    @Mock
    private JwtProvider refreshTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private ForbiddenTokenService forbiddenTokenService;

    @BeforeEach
    public void setUp() {
        accessTokenProvider = new AccessTokenProvider(secretStr, Duration.ofMinutes(5));
        jwtAuthHelper = new JwtAuthHelper(accessTokenProvider, refreshTokenProvider, refreshTokenService, forbiddenTokenService);
        userAuthUseCase = new UserAuthUseCase(userOauthSignService, jwtAuthHelper, oauthOidcHelper, accessTokenProvider);
    }

    @Test
    @DisplayName("[1] 유효하지 않으면서 만료된 토큰인 경우 ExpiredToken 예외를 던진다.")
    public void isSignedInWithExpiredToken() {
        // given
        accessTokenProvider = new AccessTokenProvider(secretStr, Duration.ofMillis(0));
        userAuthUseCase = new UserAuthUseCase(userOauthSignService, jwtAuthHelper, oauthOidcHelper, accessTokenProvider);
        String expiredToken = accessTokenProvider.generateToken(jwtClaims);

        // when
        JwtErrorException exception = assertThrows(JwtErrorException.class, () -> userAuthUseCase.isSignIn("Bearer " + expiredToken));

        // then
        assertEquals(JwtErrorCode.EXPIRED_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("[2] 유효한 토큰이면 토큰의 사용자 아이디를 반환한다.")
    public void isSignedInWithValidToken() {
        // given
        String token = accessTokenProvider.generateToken(jwtClaims);

        // when
        AuthStateDto result = userAuthUseCase.isSignIn("Bearer " + token);

        // then
        assertEquals(1L, result.id());
    }
}
