package kr.co.pennyway.domain.common.redis.sign;

import org.springframework.data.repository.ListCrudRepository;

public interface SignEventLogRepository extends ListCrudRepository<SignEventLog, Long> {
}
