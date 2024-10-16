package kr.co.pennyway.domain.common.redis.session;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.Optional;

public interface UserSessionRepository {
    void save(Long userId, String hashKey, UserSession value);

    Optional<UserSession> findUserSession(Long userId, String hashKey) throws JsonProcessingException;

    Map<String, UserSession> findAllUserSessions(Long userId) throws JsonProcessingException;

    Long getSessionTtl(Long userId, String hashKey);

    void resetSessionTtl(Long userId, String hashKey);

    void delete(Long userId, String hashKey);
}
