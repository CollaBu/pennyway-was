package kr.co.pennyway.api.apis.notification.service;

import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSearchService {
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Slice<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationService.readNotificationsSlice(userId, pageable);
    }

    @Transactional(readOnly = true)
    public boolean hasUnreadNotification(Long userId) {
        return notificationService.readUnreadNotification(userId);
    }
}
