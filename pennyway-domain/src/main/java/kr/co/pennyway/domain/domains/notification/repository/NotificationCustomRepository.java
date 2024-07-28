package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.type.Announcement;

import java.util.List;

public interface NotificationCustomRepository {
    boolean existsUnreadNotification(Long userId);

    /**
     * 사용자들에게 정기 지출 등록 알림을 저장한다. (발송이 아님)
     * 만약 이미 전송하려는 데이터가 년-월-일에 해당하는 생성일을 가지고 있고, 그 알림의 announcement 타입까지 같다면 저장하지 않는다.
     *
     * <pre>
     * {@code
     * INSERT INTO notification(type, announcement, created_at, updated_at, receiver, receiver_name)
     * SELECT ?, ?, NOW(), NOW(), u.id, u.name
     * FROM user u
     * WHERE u.id IN (?)
     * AND NOT EXISTS (
     * 	SELECT n.receiver
     * 	FROM notification n
     * 	WHERE n.receiver = u.id
     *     AND n.created_at >= CURDATE()
     *     AND n.created_at < CURDATE() + INTERVAL 1 DAY
     * 	AND n.type = '0'
     * 	AND n.announcement = 1
     * );
     * }
     * </pre>
     *
     * @param userIds      : 등록할 사용자 아이디 목록
     * @param announcement : 공지 타입 {@link Announcement}
     */
    void saveDailySpendingAnnounceInBulk(List<Long> userIds, Announcement announcement);
}
