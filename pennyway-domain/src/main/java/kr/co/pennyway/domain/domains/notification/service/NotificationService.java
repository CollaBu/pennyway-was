package kr.co.pennyway.domain.domains.notification.service;

import com.querydsl.core.types.Predicate;
import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.common.util.SliceUtil;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.domain.QNotification;
import kr.co.pennyway.domain.domains.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    private final QNotification notification = QNotification.notification;

    @Transactional(readOnly = true)
    public Slice<Notification> readNotificationsSlice(Long userId, Pageable pageable) {
        Predicate predicate = notification.receiver.id.eq(userId);

        QueryHandler queryHandler = query -> query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        Sort sort = pageable.getSort();

        return SliceUtil.toSlice(notificationRepository.findList(predicate, queryHandler, sort), pageable);
    }
}
