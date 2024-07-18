package kr.co.pennyway.api.apis.notification.usecase;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.api.apis.notification.mapper.NotificationMapper;
import kr.co.pennyway.api.apis.notification.service.NotificationSaveService;
import kr.co.pennyway.api.apis.notification.service.NotificationSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class NotificationUseCase {
    private final NotificationSearchService notificationSearchService;
    private final NotificationSaveService notificationSaveService;

    public NotificationDto.SliceRes getNotifications(Long userId, Pageable pageable) {
        Slice<Notification> notifications = notificationSearchService.getNotifications(userId, pageable);

        return NotificationMapper.toSliceRes(notifications, pageable);
    }

    public void updateNotificationsToRead(List<Long> notificationIds) {
        notificationSaveService.updateNotificationsToRead(notificationIds);
    }
}
