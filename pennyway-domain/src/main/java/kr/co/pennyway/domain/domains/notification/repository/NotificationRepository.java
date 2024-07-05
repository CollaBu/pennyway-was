package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO notification (readAt, type, announcement, receiver, sender) " +
            "SELECT false, :notification.type, :notification.announcement, :receiverId, :senderId" +
            "WHERE NOT EXISTS (SELECT 1 FROM notification " +
            "WHERE receiver = :receiverId AND DATE_FORMAT(created_at, '%Y-%m-%d') = :currentDate " +
            "AND type = :notification.type AND announcement = :notification.announcement",
            nativeQuery = true)
    void insertIfNotExistsInQuery(
            @Param("notification") Notification notification,
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId,
            @Param("currentDate") LocalDate currentDate
    );
}
