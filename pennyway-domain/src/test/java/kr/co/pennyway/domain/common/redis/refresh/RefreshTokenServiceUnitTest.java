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
        log.info("test : {}", refreshTokenRepository.findById(1L));
    }
}
