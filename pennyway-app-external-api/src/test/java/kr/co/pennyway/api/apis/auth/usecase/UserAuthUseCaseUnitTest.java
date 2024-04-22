package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserAuthUseCaseUnitTest {
    private final String secretStr = "helloMyNameIsPennywayThisIsSecretKeyItNeedsToBeLongerThan256Bits";
    private JwtProvider accessTokenProvider;
    private JwtClaims jwtClaims;
    private UserAuthUseCase userAuthUseCase;
    @Mock
    private JwtAuthHelper jwtAuthHelper;

    @BeforeEach
    public void setUp() {
        accessTokenProvider = new AccessTokenProvider(secretStr, Duration.ofMinutes(5));
        jwtClaims = AccessTokenClaim.of(1L, "ROLE_USER");
        userAuthUseCase = new UserAuthUseCase(jwtAuthHelper, accessTokenProvider);
    }

    @Test
    @DisplayName("[1] Authorication 헤더가 없으면 false를 반환한다.")
    public void isSignedInWithoutAuthorizationHeader() {
        // when
        boolean result = userAuthUseCase.isSignedIn(null);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("[2] 유효한 토큰이 아니면 false를 반환한다.")
    public void isSignedInWithInvalidToken() {
        // when
        boolean result = userAuthUseCase.isSignedIn("Bearer invalidToken");

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("[2-1] 유효하지 않으면서 만료된 토큰인 경우 ExpiredToken 예외를 던진다.")
    public void isSignedInWithExpiredToken() {
        // given
        accessTokenProvider = new AccessTokenProvider(secretStr, Duration.ofSeconds(1));
        userAuthUseCase = new UserAuthUseCase(jwtAuthHelper, accessTokenProvider);
        String expiredToken = accessTokenProvider.generateToken(jwtClaims);

        // when
        JwtErrorException exception = assertThrows(JwtErrorException.class, () -> userAuthUseCase.isSignedIn("Bearer " + expiredToken));
        assertEquals(JwtErrorCode.EXPIRED_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("[3] 유효한 토큰이면 true를 반환한다.")
    public void isSignedInWithValidToken() {
        // given
        String token = accessTokenProvider.generateToken(jwtClaims);

        // when
        boolean result = userAuthUseCase.isSignedIn("Bearer " + token);

        // then
        assertFalse(result);
    }
}
