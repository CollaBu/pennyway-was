package kr.co.pennyway.domain.common.redis.refresh;

import kr.co.pennyway.domain.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@Slf4j
@DataRedisTest(properties = "spring.config.location=classpath:application-domain.yml")
@ContextConfiguration(classes = {RedisConfig.class, RefreshTokenServiceImpl.class})
@ActiveProfiles("test")
public class RefreshTokenServiceIntegrationTest extends ContainerRedisTestConfig {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
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

    @Test
    @DisplayName("요청한 리프레시 토큰과 저장된 리프레시 토큰이 다를 경우 토큰이 탈취되었다고 판단하여 값 삭제")
    void validateTokenTest() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .token("refreshToken")
                .ttl(1000L)
                .build();
        refreshTokenService.save(refreshToken);

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> refreshTokenService.refresh(1L, "anotherRefreshToken", "newRefreshToken"));

        // then
        assertEquals("리프레시 토큰이 탈취되었을 때 예외가 발생해야 합니다.", "refresh token mismatched", exception.getMessage());
        assertFalse("리프레시 토큰이 탈취되었을 때 저장된 리프레시 토큰이 삭제되어야 합니다.", refreshTokenRepository.existsById(1L));
    }
}
