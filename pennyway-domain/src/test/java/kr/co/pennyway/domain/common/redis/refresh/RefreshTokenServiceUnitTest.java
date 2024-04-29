package kr.co.pennyway.domain.common.redis.refresh;

import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenRepository;
import kr.co.pennyway.domain.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import kr.co.pennyway.domain.config.RedisUnitTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@Slf4j
@RedisUnitTest
@DataRedisTest(properties = "spring.config.location=classpath:application-domain.yml")
@ContextConfiguration(classes = {RedisConfig.class})
@ActiveProfiles("test")
public class RefreshTokenServiceUnitTest extends ContainerRedisTestConfig {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ForbiddenTokenRepository forbiddenTokenRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        this.refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository);
    }

    @Test
    @DisplayName("리프레시 토큰 저장 테스트")
    void saveTest() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .token("refreshToken")
                .ttl(1000L)
                .build();

        // when
        refreshTokenService.save(refreshToken);

        // then
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(1L).orElse(null);
        assertEquals("저장된 리프레시 토큰이 일치하지 않습니다.", refreshToken, savedRefreshToken);
        log.info("저장된 리프레시 토큰 정보 : {}", savedRefreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰 갱신 테스트")
    void refreshTest() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .token("refreshToken")
                .ttl(1000L)
                .build();
        refreshTokenService.save(refreshToken);

        // when
        refreshTokenService.refresh(1L, "refreshToken", "newRefreshToken");

        // then
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(1L).orElse(null);
        assertEquals("갱신된 리프레시 토큰이 일치하지 않습니다.", "newRefreshToken", savedRefreshToken.getToken());
        log.info("갱신된 리프레시 토큰 정보 : {}", savedRefreshToken);
    }
}
