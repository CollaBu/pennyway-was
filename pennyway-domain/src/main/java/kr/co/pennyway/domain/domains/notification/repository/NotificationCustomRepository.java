package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.type.Announcement;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationCustomRepository {
    /**
     * 사용자들에게 정기 지출 등록 알림을 저장한다. (발송이 아님)
     * 만약 이미 publishedAt의 년-월-일에 해당하는 알림이 존재하고, 그 알림의 announcement까지 같다면 저장하지 않는다.
     *
     * <pre>
     * {@code
     * INSERT INTO notification (created_at, type, announcement, receiver)
     * SELECT
     *     'ANNOUNCEMENT' AS type,
     *     :announcement AS announcement,
     *     u.id AS receiver
     * FROM user u
     * WHERE
     *     u.id IN (:userIds)
     * AND NOT EXISTS (
     *    SELECT 1
     *    FROM notification n
     *    WHERE n.receiver = u.id
     *        AND DATE(n.created_at) = DATE(:publishedAt)
     *        AND YEAR(n.created_at) = YEAR(:publishedAt)
     *        AND n.type = 'ANNOUNCEMENT'
     *        AND n.announcement = :announcement
     * );
     * }
     * </pre>
     *
     * @param userIds      : 등록할 사용자 아이디 목록
     * @param publishedAt  : 알림 발송 시간, 공지 알림 중복 저장 방지를 위해 조건식에 사용
     * @param announcement : 알림 타입 {@link Announcement}
     */
    void saveDailySpendingAnnounceInBulk(List<Long> userIds, LocalDateTime publishedAt, Announcement announcement);
}
