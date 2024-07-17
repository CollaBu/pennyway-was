package kr.co.pennyway.api.apis.notification.mapper;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
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
        String title = "", content = "";

        // 1. notification의 타입을 확인
        NoticeType type = notification.getType();

//        // 2. type이 Announcement라면, Announcement도 확인한다.
//        if (type.equals(NoticeType.ANNOUNCEMENT)) { // TODO: 공지사항 중에 title, content 모두, 한쪽, 혹은 아예 이름이 들어가지 않는 경우가 있을 수 있음.
//            Announcement announcement = notification.getAnnouncement();
//            title = announcement.createFormattedTitle("헬로"); // TODO: 사용자 이름을 받아서 처리
//            content = announcement.createFormattedContent("헬로"); // TODO: 사용자 이름을 받아서 처리
//        } else { // TODO: 공지사항이 아닌 경우도, 알림 종류에 따라 title, content의 형태가 어떻게 될 지 알 수가 없음.
//            title = type.createFormattedTitle("헬로"); // TODO: 사용자 이름을 받아서 처리
//            content = type.createFormattedContent("헬로"); // TODO: 사용자 이름을 받아서 처리
//        }

        return NotificationDto.Info.builder()
                .id(notification.getId())
                .title(title)
                .content(content)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
