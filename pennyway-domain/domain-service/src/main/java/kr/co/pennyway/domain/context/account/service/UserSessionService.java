package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.session.domain.UserSession;
import kr.co.pennyway.domain.domains.session.service.UserSessionRedisService;
import kr.co.pennyway.domain.domains.session.type.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class UserSessionService {
    private final UserSessionRedisService userSessionRedisService;

    public void create(Long userId, String deviceId, UserSession value) {
        userSessionRedisService.create(userId, deviceId, value);
    }

    public Optional<UserSession> read(Long userId, String deviceId) {
        return userSessionRedisService.read(userId, deviceId);
    }

    public Map<String, UserSession> readAll(Long userId) {
        return userSessionRedisService.readAll(userId);
    }

    public boolean isExists(Long userId, String deviceId) {
        return userSessionRedisService.isExists(userId, deviceId);
    }

    public UserSession updateUserStatus(Long userId, String deviceId, UserStatus status) {
        return userSessionRedisService.updateUserStatus(userId, deviceId, status);
    }

    public UserSession updateUserStatus(Long userId, String deviceId, Long chatRoomId) {
        return userSessionRedisService.updateUserStatus(userId, deviceId, chatRoomId);
    }

    public Long getSessionTtl(Long userId, String deviceId) {
        return userSessionRedisService.getSessionTtl(userId, deviceId);
    }
}
