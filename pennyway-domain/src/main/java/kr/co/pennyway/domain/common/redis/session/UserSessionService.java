package kr.co.pennyway.domain.common.redis.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.pennyway.common.annotation.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;

    public void create(Long userId, String hashKey, UserSession value) {
        userSessionRepository.save(userId, hashKey, value);
    }

    public Optional<UserSession> read(Long userId, String hashKey) throws JsonProcessingException {
        return userSessionRepository.findUserSession(userId, hashKey);
    }

    public Map<String, UserSession> readAll(Long userId) throws JsonProcessingException {
        return userSessionRepository.findAllUserSessions(userId);
    }

    public Long getSessionTtl(Long userId, String hashKey) {
        return userSessionRepository.getSessionTtl(userId, hashKey);
    }

    public void resetSessionTtl(Long userId, String hashKey) {
        userSessionRepository.resetSessionTtl(userId, hashKey);
    }

    public void delete(Long userId, String hashKey) {
        userSessionRepository.delete(userId, hashKey);
    }
}
