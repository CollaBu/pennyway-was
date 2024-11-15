package kr.co.pennyway.domain.common.redis.refresh;

public interface RefreshTokenCustomRepository {
    void deleteAllByUserId(Long userId);
}
