package kr.co.pennyway.domain.common.redis.session;

import java.util.Map;
import java.util.Optional;

public interface UserSessionRepository {
    void save(Long userId, String hashKey, UserSession value);

    Optional<UserSession> findUserSession(Long userId, String hashKey);

    Map<String, UserSession> findAllUserSessions(Long userId);

    Long getSessionTtl(Long userId, String hashKey);

    void resetSessionTtl(Long userId, String hashKey);

    void delete(Long userId, String hashKey);
}
