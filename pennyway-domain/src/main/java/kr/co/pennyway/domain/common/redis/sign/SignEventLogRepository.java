package kr.co.pennyway.domain.common.redis.sign;

import org.springframework.data.repository.CrudRepository;

public interface SignEventLogRepository extends CrudRepository<SignEventLog, Long> {
}
