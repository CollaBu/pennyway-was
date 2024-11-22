package kr.co.pennyway.domain.domains.notification.service;

import com.querydsl.core.types.Predicate;
import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.common.util.SliceUtil;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.domain.QNotification;
import kr.co.pennyway.domain.domains.notification.repository.NotificationRepository;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class NotificationRdbService {
    private final NotificationRepository notificationRepository;

    private final QNotification notification = QNotification.notification;

    @Transactional(readOnly = true)
    public Slice<Notification> readNotificationsSlice(Long userId, Pageable pageable, NoticeType noticeType) {
        Predicate predicate = notification.receiver.id.eq(userId)
                .and(notification.readAt.isNotNull())
                .and(notification.type.eq(noticeType));

        QueryHandler queryHandler = query -> query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        Sort sort = pageable.getSort();

        return SliceUtil.toSlice(notificationRepository.findList(predicate, queryHandler, sort), pageable);
    }

    @Transactional(readOnly = true)
    public List<Notification> readUnreadNotifications(Long userId, NoticeType noticeType) {
        Predicate predicate = notification.receiver.id.eq(userId)
                .and(notification.readAt.isNull())
                .and(notification.type.eq(noticeType));

        return notificationRepository.findList(predicate, null, null);
    }

    @Transactional(readOnly = true)
    public boolean isExistsUnreadNotification(Long userId) {
        return notificationRepository.existsUnreadNotification(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId, List<Long> notificationIds) {
        return notificationRepository.countUnreadNotificationsByIds(userId, notificationIds);
    }

    @Transactional
    public void updateReadAtByIdsInBulk(List<Long> notificationIds) {
        notificationRepository.updateReadAtByIdsInBulk(notificationIds);
    }
}
