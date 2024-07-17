package kr.co.pennyway.api.apis.notification.mapper;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@Slf4j
@Mapper
public class NotificationMapper {
    /**
     * Slice<Notification> 타입을 무한 스크롤 응답 형태로 변환한다.
     */
    public static NotificationDto.SliceRes toSliceRes(Slice<Notification> notifications, Pageable pageable) {
        return NotificationDto.SliceRes.from(
                notifications.getContent().stream().map(NotificationMapper::toRes).toList(),
                pageable,
                notifications.getNumberOfElements(),
                notifications.hasNext()
        );
    }

    /**
     * Notification 정보를 추출하여 응답 형태로 변환한다.
     */
    private static NotificationDto.Info toRes(Notification notification) {
        String title = notification.createFormattedTitle();
        String content = notification.createFormattedContent();

        return NotificationDto.Info.builder()
                .id(notification.getId())
                .title(title)
                .content(content)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
