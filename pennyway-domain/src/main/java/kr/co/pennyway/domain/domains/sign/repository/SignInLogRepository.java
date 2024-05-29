package kr.co.pennyway.domain.domains.sign.repository;

import kr.co.pennyway.domain.domains.sign.domain.SignInLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignInLogRepository extends JpaRepository<SignInLog, Long> {
    
}
