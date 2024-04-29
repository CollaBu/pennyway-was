package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenProvider;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenClaim;
import kr.co.pennyway.api.common.security.jwt.refresh.RefreshTokenProvider;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshToken;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenRepository;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenService;
import kr.co.pennyway.domain.common.redis.refresh.RefreshTokenServiceImpl;
import kr.co.pennyway.domain.config.RedisConfig;
import kr.co.pennyway.domain.config.RedisUnitTest;
import kr.co.pennyway.domain.domains.user.type.Role;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@Slf4j
@ExtendWith(MockitoExtension.class)
@RedisUnitTest
@DataRedisTest(properties = "spring.config.location=classpath:application-domain.yml")
@ContextConfiguration(classes = {RedisConfig.class, JwtAuthHelper.class})
@ActiveProfiles("test")
public class JwtAuthHelperTest extends ExternalApiDBTestConfig {
    @Autowired
    private JwtAuthHelper jwtAuthHelper;

    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private AccessTokenProvider accessTokenProvider;

    @MockBean
    private RefreshTokenProvider refreshTokenProvider;

    @MockBean
    private ForbiddenTokenService forbiddenTokenService;

    @BeforeEach
    void setUp() {
        this.refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository);
    }

    @Test
    @DisplayName("사용자 아이디에 해당하는 리프레시 토큰이 존재할 시, 리프레시 토큰 갱신에 성공한다.")
    public void RefreshTokenRefreshSuccess() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .token("refreshToken")
                .ttl(1000L)
                .build();
        refreshTokenRepository.save(refreshToken);
        given(refreshTokenProvider.getJwtClaimsFromToken(refreshToken.getToken())).willReturn(RefreshTokenClaim.of(refreshToken.getUserId(), Role.USER.getType()));
        given(accessTokenProvider.generateToken(any())).willReturn("newAccessToken");
        given(refreshTokenProvider.generateToken(any())).willReturn("newRefreshToken");

        // when
        Pair<Long, Jwts> jwts = jwtAuthHelper.refresh(refreshToken.getToken());

        // then
        assertEquals("사용자 아이디가 일치하지 않습니다.", refreshToken.getUserId(), jwts.getLeft());
        assertEquals("갱신된 액세스 토큰이 일치하지 않습니다.", "newAccessToken", jwts.getRight().accessToken());
        assertEquals("리프레시 토큰이 갱신되지 않았습니다.", "newRefreshToken", jwts.getRight().refreshToken());
        log.info("갱신된 리프레시 토큰 정보 : {}", refreshTokenRepository.findById(refreshToken.getUserId()).orElse(null));
    }

    @Test
    @DisplayName("사용자 아이디에 해당하는 다른 리프레시 토큰이 저장되어 있을 시, 탈취되었다고 판단하고 토큰을 제거한 후 JwtErrorException을 발생시킨다.")
    public void RefreshTokenRefreshFail() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .token("refreshToken")
                .ttl(1000L)
                .build();
        refreshTokenRepository.save(refreshToken);

        given(refreshTokenProvider.getJwtClaimsFromToken("anotherRefreshToken")).willReturn(RefreshTokenClaim.of(refreshToken.getUserId(), Role.USER.toString()));
        given(refreshTokenProvider.generateToken(any())).willReturn("newRefreshToken");

        // when
        JwtErrorException jwtErrorException = assertThrows(JwtErrorException.class, () -> jwtAuthHelper.refresh("anotherRefreshToken"));

        // then
        assertEquals("탈취 시나리오 예외가 발생하지 않았습니다.", JwtErrorCode.TAKEN_AWAY_TOKEN, jwtErrorException.getErrorCode());
        assertFalse("리프레시 토큰이 삭제되지 않았습니다.", refreshTokenRepository.existsById(refreshToken.getUserId()));
    }
}
