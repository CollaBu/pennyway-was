package kr.co.pennyway.common.security.jwt.access;

import kr.co.infra.common.jwt.JwtClaims;
import kr.co.infra.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AccessTokenProviderTest {
    private final String secretStr = "helloMyNameIsPennywayThisIsSecretKeyItNeedsToBeLongerThan256Bits";
    private JwtProvider jwtProvider;
    private JwtClaims jwtClaims;

    @BeforeEach
    public void setUp() {
        jwtProvider = new AccessTokenProvider(secretStr, Duration.ofMinutes(5));
        jwtClaims = AccessTokenClaim.of("1", "ROLE_USER");
    }

    @Test
    @DisplayName("토큰 생성이 정상적으로 이루어지는지 확인한다.")
    public void createToken() {
        // when
        String token = jwtProvider.generateToken(jwtClaims);

        // then
        assertNotNull(token);
        System.out.println(token);
    }

    @Test
    @DisplayName("토큰에서 정보를 추출할 수 있는지 확인한다.")
    public void getSubInfoFromToken() {
        // given
        String token = jwtProvider.generateToken(jwtClaims);

        // when
        JwtClaims subInfo = jwtProvider.getSubInfoFromToken(token);

        // then
        assertNotNull(subInfo);
        System.out.println(subInfo.getClaims());
    }

    @Test
    @DisplayName("토큰의 만료일을 확인한다.")
    public void getExpiryDate() {
        // given
        String token = jwtProvider.generateToken(jwtClaims);

        // when
        jwtProvider.getExpiryDate(token);

        // then
        assertNotNull(jwtProvider.getExpiryDate(token));
        System.out.println(jwtProvider.getExpiryDate(token));
    }

    @Test
    @DisplayName("토큰의 만료 여부를 확인한다.")
    public void isTokenExpired() {
        // given
        String token = jwtProvider.generateToken(jwtClaims);

        // when
        jwtProvider.isTokenExpired(token);

        // then
        assertFalse(jwtProvider.isTokenExpired(token));
    }
}
