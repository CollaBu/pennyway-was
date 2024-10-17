package kr.co.pennyway.domain.common.redis.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.common.annotation.DomainRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
        executeScript(SessionLuaScripts.SAVE, userId, hashKey, serialize(value), ttlSeconds);
    }

    @Override
    public Optional<UserSession> findUserSession(Long userId, String hashKey) {
        Object result = executeScript(SessionLuaScripts.FIND, userId, hashKey);

        return Optional.ofNullable(deserialize(result));
    }

    @Override
    public Map<String, UserSession> findAllUserSessions(Long userId) {
        List<Object> result = executeScript(SessionLuaScripts.FIND_ALL, userId);

        return deserializeMap(result);
    }

    @Override
    public Long getSessionTtl(Long userId, String hashKey) {
        return executeScript(SessionLuaScripts.GET_TTL, userId, hashKey);
    }

    @Override
    public boolean exists(Long userId, String hashKey) {
        return executeScript(SessionLuaScripts.EXISTS, userId, hashKey);
    }

    @Override
    public void resetSessionTtl(Long userId, String hashKey) {
        executeScript(SessionLuaScripts.RESET_TTL, userId, hashKey, ttlSeconds);
    }

    @Override
    public void delete(Long userId, String hashKey) {
        executeScript(SessionLuaScripts.DELETE, userId, hashKey);
    }

    private String createKey(Long userId) {
        return "user:" + userId;
    }

    private <T> T executeScript(SessionLuaScripts script, Long userId, Object... args) {
        try {
            return redisTemplate.execute(
                    script.getScript(),
                    List.of(createKey(userId)),
                    args
            );
        } catch (Exception e) {
            log.error("Error executing Redis script: {}", script.name(), e);
            throw new RuntimeException("Failed to execute Redis operation", e);
        }
    }

    private String serialize(UserSession value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Error serializing UserSession", e);
            throw new RuntimeException("Failed to serialize UserSession", e);
        }
    }

    private UserSession deserialize(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.readValue((String) value, UserSession.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing UserSession", e);
            throw new RuntimeException("Failed to deserialize UserSession", e);
        }
    }

    private Map<String, UserSession> deserializeMap(List<Object> entries) {
        Map<String, UserSession> result = new ConcurrentHashMap<>();
        for (int i = 0; i < entries.size(); i += 2) {
            String key = (String) entries.get(i);
            UserSession value = deserialize(entries.get(i + 1));
            result.put(key, value);
        }
        return result;
    }
}
