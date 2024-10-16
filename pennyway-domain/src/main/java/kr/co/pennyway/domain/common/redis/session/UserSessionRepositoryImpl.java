package kr.co.pennyway.domain.common.redis.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.common.annotation.DomainRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class UserSessionRepositoryImpl implements UserSessionRepository {
    private static final long ttlSeconds = 60 * 60 * 24 * 7; // 1주일 (초)
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, UserSession> redisTemplate;

    public UserSessionRepositoryImpl(@DomainRedisTemplate RedisTemplate<String, UserSession> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(Long userId, String hashKey, UserSession value) {
        String key = createKey(userId);

        String luaScript =
                "redis.call('HSET', KEYS[1], ARGV[1], ARGV[2]) " +
                        "return redis.call('HEXPIRE', KEYS[1], ARGV[3], 'FIELDS', '1', ARGV[1])";

        RedisScript<List> script = RedisScript.of(luaScript, List.class);

        try {
            List<Object> result = redisTemplate.execute(script,
                    List.of(key),
                    hashKey,
                    serialize(value),
                    ttlSeconds
            );

            log.info("User session saved for user {} with hash key {}. Result: {}", userId, hashKey, result);
        } catch (Exception e) {
            log.error("Error saving user session for user {} with hash key {}", userId, hashKey, e);
            throw new RuntimeException("Failed to save user session", e);
        }
    }

    @Override
    public Optional<UserSession> findUserSession(Long userId, String hashKey) throws JsonProcessingException {
        String luaScript =
                "return redis.call('HGET', KEYS[1], ARGV[1])";

        RedisScript<Object> script = RedisScript.of(luaScript, Object.class);

        try {
            Object result = redisTemplate.execute(script,
                    List.of(createKey(userId)),
                    hashKey
            );

            log.info("User session found for user {} with hash key {}: {}", userId, hashKey, result);

            UserSession session = deserializeFromBase64((String) result);
            log.debug("User session found for user {} with hash key {}: {}", userId, hashKey, session);

            return Optional.ofNullable(session);
        } catch (Exception e) {
            log.error("Error finding user session for user {} with hash key {}", userId, hashKey, e);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, UserSession> findAllUserSessions(Long userId) throws JsonProcessingException {
        String luaScript =
                "return redis.call('HGETALL', KEYS[1])";

        RedisScript<List> script = RedisScript.of(luaScript, List.class);

        try {
            List<Object> result = redisTemplate.execute(script,
                    List.of(createKey(userId))
            );

            Map<String, UserSession> sessions = new ConcurrentHashMap<>();
            for (int i = 0; i < result.size(); i += 2) {
                String key = result.get(i).toString();
                UserSession value = deserializeFromBase64((String) result.get(i + 1));
                log.debug("User session found for user {} with hash key {}: {}", userId, key, value);
                sessions.put(key, value);
            }

            return sessions;
        } catch (Exception e) {
            log.error("Error finding all user sessions for user {}", userId, e);
            return new ConcurrentHashMap<>();
        }
    }

    @Override
    public Long getSessionTtl(Long userId, String hashKey) {
        String luaScript =
                "return redis.call('HTTL', KEYS[1], 'FIELDS', '1', ARGV[1])";

        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);

        try {
            Long result = redisTemplate.execute(script,
                    List.of(createKey(userId)),
                    hashKey
            );

            return result != null ? result : -1L;
        } catch (Exception e) {
            log.error("Error getting session TTL for user {} with hash key {}", userId, hashKey, e);
            return -1L;
        }
    }

    @Override
    public void resetSessionTtl(Long userId, String hashKey) {
        String luaScript =
                "return redis.call('HEXPIRE', KEYS[1], ARGV[1], 'FIELDS', '1', ARGV[2])";

        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);

        try {
            Long result = redisTemplate.execute(script,
                    List.of(createKey(userId)),
                    ttlSeconds,
                    hashKey
            );

            log.debug("Reset session TTL for user {} with hash key {}. Result: {}", userId, hashKey, result);
        } catch (Exception e) {
            log.error("Error resetting session TTL for user {} with hash key {}", userId, hashKey, e);
            throw new RuntimeException("Failed to reset session TTL", e);
        }
    }

    @Override
    public void delete(Long userId, String hashKey) {
        String luaScript =
                "return redis.call('HDEL', KEYS[1], ARGV[1])";

        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);

        try {
            Long result = redisTemplate.execute(script,
                    List.of(createKey(userId)),
                    hashKey
            );

            log.debug("Deleted session for user {} with hash key {}. Result: {}", userId, hashKey, result);
        } catch (Exception e) {
            log.error("Error deleting session for user {} with hash key {}", userId, hashKey, e);
            throw new RuntimeException("Failed to delete session", e);
        }
    }

    private byte[] serialize(UserSession value) {
        try {
            RedisSerializer<UserSession> valueSerializer = (RedisSerializer<UserSession>) redisTemplate.getValueSerializer();
            return valueSerializer.serialize(value);
        } catch (Exception e) {
            log.error("Error serializing UserSession", e);
            throw new RuntimeException("Failed to serialize UserSession", e);
        }
    }

    private UserSession deserializeFromBase64(String base64String) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonString, UserSession.class);
    }

    private String createKey(Long userId) {
        return "user:" + userId;
    }
}
