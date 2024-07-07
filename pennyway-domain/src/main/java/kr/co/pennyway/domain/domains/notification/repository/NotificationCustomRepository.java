package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.type.Announcement;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationCustomRepository {
    /**
     * 사용자들에게 정기 지출 등록 알림을 저장한다.
     *
     * @param userIds      : 등록할 사용자 아이디 목록
     * @param publishedAt  : 알림 발송 시간, 공지 알림 중복 저장 방지를 위해 조건식에 사용
     * @param announcement : 알림 타입 {@link Announcement}
     */
    void saveDailySpendingAnnounceInBulk(List<Long> userIds, LocalDateTime publishedAt, Announcement announcement);
}
