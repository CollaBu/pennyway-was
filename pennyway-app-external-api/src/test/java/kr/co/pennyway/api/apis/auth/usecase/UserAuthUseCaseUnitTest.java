package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.AuthStateDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserAuthUseCaseUnitTest {
    private final String secretStr = "helloMyNameIsPennywayThisIsSecretKeyItNeedsToBeLongerThan256Bits";
    private final JwtClaims jwtClaims = AccessTokenClaim.of(1L, "ROLE_USER");
    private JwtProvider accessTokenProvider;
    private UserAuthUseCase userAuthUseCase;
    @Mock
    private JwtAuthHelper jwtAuthHelper;

    @BeforeEach
    public void setUp() {
        accessTokenProvider = new AccessTokenProvider(secretStr, Duration.ofMinutes(5));
        userAuthUseCase = new UserAuthUseCase(jwtAuthHelper, accessTokenProvider);
    }

    @Test
    @DisplayName("[1] Authorication 헤더가 없으면 false를 반환한다.")
    public void isSignedInWithoutAuthorizationHeader() {
        // when
        AuthStateDto result = userAuthUseCase.isSignIn("");

        // then
        assertFalse(result.isSignIn());
        assertNull(result.userId());
    }

    @Test
    @DisplayName("[2] 유효한 토큰이 아니면 예외를 반환한다.")
    public void isSignedInWithInvalidToken() {
        // when
        JwtErrorException exception = assertThrows(JwtErrorException.class, () -> userAuthUseCase.isSignIn("Bearer invalidToken"));

        // then
        System.out.println(exception.getErrorCode());
    }

    @Test
    @DisplayName("[2-1] 유효하지 않으면서 만료된 토큰인 경우 ExpiredToken 예외를 던진다.")
    public void isSignedInWithExpiredToken() {
        // given
        accessTokenProvider = new AccessTokenProvider(secretStr, Duration.ofMillis(0));
        userAuthUseCase = new UserAuthUseCase(jwtAuthHelper, accessTokenProvider);
        String expiredToken = accessTokenProvider.generateToken(jwtClaims);

        // when
        JwtErrorException exception = assertThrows(JwtErrorException.class, () -> userAuthUseCase.isSignIn("Bearer " + expiredToken));

        // then
        assertEquals(JwtErrorCode.EXPIRED_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("[3] 유효한 토큰이면 true를 반환한다.")
    public void isSignedInWithValidToken() {
        // given
        String token = accessTokenProvider.generateToken(jwtClaims);
        given(jwtAuthHelper.getClaimValue(any(), any(), any())).willReturn(1L);

        // when
        AuthStateDto result = userAuthUseCase.isSignIn("Bearer " + token);

        // then
        assertTrue(result.isSignIn());
        assertEquals(1L, result.userId());
    }
}
