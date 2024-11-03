package kr.co.pennyway.domain.domains.chatstatus.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatMessageStatusRedisRepository implements ChatMessageStatusCacheRepository {
    private static final String CACHE_KEY_PREFIX = "chat:last_read:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<Long> findLastReadMessageId(Long userId, Long chatRoomId) {
        String value = redisTemplate.opsForValue().get(formatCacheKey(userId, chatRoomId));
        return Optional.ofNullable(value).map(Long::parseLong);
    }

    @Override
    public void saveLastReadMessageId(Long userId, Long chatRoomId, Long messageId) {
        try {
            String key = formatCacheKey(userId, chatRoomId);
            String currentValue = redisTemplate.opsForValue().get(key);

            if (currentValue != null && Long.parseLong(currentValue) >= messageId) {
                return;
            }

            redisTemplate.opsForValue().set(key, messageId.toString());
            redisTemplate.expire(key, CACHE_TTL);
        } catch (Exception e) {
            log.error("Failed to cache message status: userId={}, roomId={}, messageId={}", userId, chatRoomId, messageId, e);
        }
    }

    @Override
    public void deleteLastReadMessageId(Long userId, Long chatRoomId) {
        redisTemplate.delete(formatCacheKey(userId, chatRoomId));
    }

    private String formatCacheKey(Long userId, Long chatRoomId) {
        return CACHE_KEY_PREFIX + chatRoomId + ":" + userId;
    }
}
