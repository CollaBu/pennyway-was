package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaim;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

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
}
