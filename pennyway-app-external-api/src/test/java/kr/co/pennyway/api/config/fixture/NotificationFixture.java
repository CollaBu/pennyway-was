package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public enum NotificationFixture {
    ANNOUNCEMENT_DAILY_SPENDING(null, NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING);

    private final LocalDateTime readAt;
    private final NoticeType type;
    private final Announcement announcement;

    public Notification toEntity(User receiver) {
        return new Notification.Builder(this.type, this.announcement, receiver)
                .build();
    }
}
