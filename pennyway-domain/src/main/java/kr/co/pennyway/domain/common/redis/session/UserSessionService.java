package kr.co.pennyway.domain.common.redis.session;

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

    public void create(Long userId, String domainId, UserSession value) {
        userSessionRepository.save(userId, domainId, value);
    }

    public Optional<UserSession> read(Long userId, String domainId) {
        return userSessionRepository.findUserSession(userId, domainId);
    }

    public Map<String, UserSession> readAll(Long userId) {
        return userSessionRepository.findAllUserSessions(userId);
    }

    public boolean isExists(Long userId, String domainId) {
        Optional<UserSession> userSession = userSessionRepository.findUserSession(userId, domainId);

        return userSession.isPresent();
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
