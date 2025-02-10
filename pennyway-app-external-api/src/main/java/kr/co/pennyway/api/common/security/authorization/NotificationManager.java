package kr.co.pennyway.api.common.security.authorization;

import kr.co.pennyway.domain.context.alter.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component("notificationManager")
@RequiredArgsConstructor
public class NotificationManager {
    private final NotificationService notificationService;

    /**
     * 사용자가 알림 리스트에 대한 전체 접근 권한이 있는지 확인한다.
     * <p>
     * 조회 결과와 요청 파라미터의 개수가 동일해야 하며, 읽음 상태의 알림은 포함되어선 안 된다.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, List<Long> notificationIds) {
        return notificationService.countUnreadNotifications(userId, notificationIds) == (long) notificationIds.size();
    }
}
