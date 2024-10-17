package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.common.redis.session.UserSession;
import kr.co.pennyway.domain.common.redis.session.UserSessionService;
import kr.co.pennyway.socket.common.dto.StatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {
    private final UserSessionService userSessionService;

    public void updateStatus(Long userId, String deviceId, StatusMessage message) {
        if (message.isChatRoomStatus()) {
            UserSession session = userSessionService.updateUserStatus(userId, deviceId, message.chatRoomId());
            log.debug("사용자 상태 변경: {}", session);
        } else {
            UserSession session = userSessionService.updateUserStatus(userId, deviceId, message.status());
            log.debug("사용자 상태 변경: {}", session);
        }
    }
}
