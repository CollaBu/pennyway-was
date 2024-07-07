package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationCustomRepository {
    void saveDailySpendingAnnounceInBulk(List<Long> userIds, LocalDateTime publishedAt, NoticeType noticeType, Announcement announcement);
}
