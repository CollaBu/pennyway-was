package kr.co.pennyway.domain.context.alter.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.service.NotificationRdbService;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRdbService notificationRdbService;

    @Transactional(readOnly = true)
    public Slice<Notification> readNotifications(Long userId, Pageable pageable, NoticeType noticeType) {
        return notificationRdbService.readNotificationsSlice(userId, pageable, noticeType);
    }

    @Transactional(readOnly = true)
    public List<Notification> readUnreadNotifications(Long userId, NoticeType noticeType) {
        return notificationRdbService.readUnreadNotifications(userId, noticeType);
    }

    @Transactional(readOnly = true)
    public boolean isExistsUnreadNotification(Long userId) {
        return notificationRdbService.isExistsUnreadNotification(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId, List<Long> notificationIds) {
        return notificationRdbService.countUnreadNotifications(userId, notificationIds);
    }

    @Transactional
    public void updateReadAtByIds(List<Long> notificationIds) {
        notificationRdbService.updateReadAtByIdsInBulk(notificationIds);
    }
}
