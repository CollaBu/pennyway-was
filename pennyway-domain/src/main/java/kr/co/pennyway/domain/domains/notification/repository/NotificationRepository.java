package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.notification.domain.Notification;

public interface NotificationRepository extends ExtendedRepository<Notification, Long>, NotificationCustomRepository {
}
