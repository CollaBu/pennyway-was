package kr.co.pennyway.api.apis.notification.mapper;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Mapper
public class NotificationMapper {
    /**
     * Notification 타입을 NotificationDto.Info 타입으로 변환한다.
     */
    public static List<NotificationDto.Info> toInfoList(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationDto.Info::from)
                .sorted(Comparator.comparing(NotificationDto.Info::id).reversed())
                .toList();
    }

    /**
     * Slice<Notification> 타입을 무한 스크롤 응답 형태로 변환한다.
     */
    public static NotificationDto.SliceRes toSliceRes(Slice<Notification> notifications, Pageable pageable) {
        return NotificationDto.SliceRes.from(
                notifications.getContent().stream().map(NotificationDto.Info::from).toList(),
                pageable,
                notifications.getNumberOfElements(),
                notifications.hasNext()
        );
    }
}
