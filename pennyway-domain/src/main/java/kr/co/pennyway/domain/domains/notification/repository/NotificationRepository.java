package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
