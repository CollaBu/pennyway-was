package kr.co.pennyway.domain.common.redis.sign;

import kr.co.pennyway.common.annotation.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SignEventLogService {
    private final SignEventLogRepository signEventLogRepository;

    public void create(SignEventLog signEventLog) {
        signEventLogRepository.save(signEventLog);
        log.debug("로그 저장 : {}", signEventLog);
    }

    public List<SignEventLog> findAll() {
        return signEventLogRepository.findAll();
    }
}
