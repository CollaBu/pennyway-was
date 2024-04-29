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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.assertEquals;

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
    public void RefreshTokenRefreshSuccess() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .token("refreshToken")
                .ttl(1000L)
                .build();
        refreshTokenRepository.save(refreshToken);
        given(refreshTokenProvider.getJwtClaimsFromToken(refreshToken.getToken())).willReturn(RefreshTokenClaim.of(refreshToken.getUserId(), refreshToken.getToken()));
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
}
