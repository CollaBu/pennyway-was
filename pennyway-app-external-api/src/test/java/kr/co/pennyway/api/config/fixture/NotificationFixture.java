package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public enum NotificationFixture {
    ANNOUNCEMENT_DAILY_SPENDING(null, NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING);

    private final LocalDateTime readAt;
    private final NoticeType type;
    private final Announcement announcement;

    public static NotificationDto.SliceRes createSliceRes(Pageable pa, int currentPageNumber, int numberOfElements) {
        return new NotificationDto.SliceRes(
                List.of(createInfo(1L, "title", "content", ANNOUNCEMENT_DAILY_SPENDING)),
                currentPageNumber,
                pa.getPageSize(),
                numberOfElements,
                false
        );
    }

    public static NotificationDto.Info createInfo(Long id, String title, String content, NotificationFixture fixture) {
        return new NotificationDto.Info(
                id,
                (fixture.readAt != null),
                title,
                content,
                fixture.type.name(),
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public Notification toEntity() {
        return new Notification.Builder(this.type, this.announcement)
                .readAt(this.readAt)
                .build();
    }
}
