package kr.co.pennyway.domain.domains.refresh.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefreshTokenCustomRepositoryImpl implements RefreshTokenCustomRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteAllByUserId(Long userId) {
        String pattern = "refreshToken:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
