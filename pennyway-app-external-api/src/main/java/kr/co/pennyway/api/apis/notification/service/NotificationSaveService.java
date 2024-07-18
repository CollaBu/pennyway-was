package kr.co.pennyway.api.apis.notification.service;

import kr.co.pennyway.domain.domains.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSaveService {
    private final NotificationService notificationService;

    /**
     * 알림 목록을 읽음 상태로 업데이트합니다.
     *
     * @param notificationIds 읽음 처리할 알림 ID 목록
     */
    public void updateNotificationsToRead(Long userId, List<Long> notificationIds) {
        notificationService.updateReadAtByIdsInBulk(notificationIds);
    }
}
