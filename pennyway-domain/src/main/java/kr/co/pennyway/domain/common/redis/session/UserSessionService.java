package kr.co.pennyway.domain.common.redis.session;

import kr.co.pennyway.common.annotation.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;

    public void create(Long userId, String deviceId, UserSession value) {
        userSessionRepository.save(userId, deviceId, value);
    }

    public Optional<UserSession> read(Long userId, String deviceId) {
        return userSessionRepository.findUserSession(userId, deviceId);
    }

    public Map<String, UserSession> readAll(Long userId) {
        return userSessionRepository.findAllUserSessions(userId);
    }

    public boolean isExists(Long userId, String deviceId) {
        return userSessionRepository.exists(userId, deviceId);
    }

    /**
     * 사용자 세션의 상태를 변경합니다.
     * {@link UserStatus#ACTIVE_CHAT_ROOM} 이외에 사용자 세션의 상태를 변경할 때 사용합니다.
     * 사용자 세션의 chatRoomId는 -1로 설정됩니다.
     *
     * @throws IllegalArgumentException 사용자 세션 정보를 찾을 수 없을 때 발생합니다.fix
     */
    public UserSession updateUserStatus(Long userId, String deviceId, UserStatus status) {
        return updateUserStatus(userId, deviceId, -1L, status);
    }

    /**
     * 사용자 세션의 상태를 변경합니다.
     * {@link UserStatus#ACTIVE_CHAT_ROOM}로 변경할 때 사용되며, chatRoomId는 null 혹은 0을 포함한 음수를 허용하지 않습니다.
     *
     * @throws IllegalArgumentException chatRoomId가 null 혹은 0을 포함한 음수일 때 발생합니다. 사용자 세션 정보를 찾을 수 없을 때도 발생합니다.
     */
    public UserSession updateUserStatus(Long userId, String deviceId, Long chatRoomId) {
        if (Objects.isNull(chatRoomId) || chatRoomId <= 0) {
            throw new IllegalArgumentException("채팅방 ID는 null 혹은 0을 포함한 음수를 허용하지 않습니다.");
        }

        return updateUserStatus(userId, deviceId, chatRoomId, UserStatus.ACTIVE_APP);
    }

    private UserSession updateUserStatus(Long userId, String deviceId, Long chatRoomId, UserStatus status) {
        UserSession userSession = userSessionRepository.findUserSession(userId, deviceId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 세션을 찾을 수 없습니다."));

        userSession.updateStatus(status, chatRoomId);
        userSessionRepository.save(userId, deviceId, userSession);

        return userSession;
    }

    public Long getSessionTtl(Long userId, String deviceId) {
        return userSessionRepository.getSessionTtl(userId, deviceId);
    }

    public void resetSessionTtl(Long userId, String deviceId) {
        userSessionRepository.resetSessionTtl(userId, deviceId);
    }

    public void delete(Long userId, String deviceId) {
        userSessionRepository.delete(userId, deviceId);
    }
}
