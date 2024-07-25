package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends ExtendedRepository<Notification, Long>, NotificationCustomRepository {
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Notification n set n.readAt = current_timestamp where n.id in ?1")
    void updateReadAtByIdsInBulk(List<Long> notificationIds);

    @Transactional(readOnly = true)
    boolean existsTopByReceiver_IdAndReadAtIsNull(Long userId);

    @Transactional(readOnly = true)
    @Query("select count(n) from Notification n where n.receiver.id = ?1 and n.id in ?2 and n.readAt is null")
    long countUnreadNotificationsByIds(Long userId, List<Long> notificationIds);
}
