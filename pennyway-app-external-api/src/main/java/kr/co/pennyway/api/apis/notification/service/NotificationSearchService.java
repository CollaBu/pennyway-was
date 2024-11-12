package kr.co.pennyway.api.apis.notification.service;

import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.service.NotificationService;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSearchService {
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Slice<Notification> getAnnounceNotifications(Long userId, Pageable pageable) {
        return notificationService.readNotificationsSlice(userId, pageable, NoticeType.ANNOUNCEMENT);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAnnounceUnreadNotifications(Long userId) {
        return notificationService.readUnreadNotifications(userId, NoticeType.ANNOUNCEMENT);
    }

    @Transactional(readOnly = true)
    public boolean isExistsUnreadNotification(Long userId) {
        return notificationService.isExistsUnreadNotification(userId);
    }
}
