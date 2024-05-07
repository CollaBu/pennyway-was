package kr.co.pennyway.domain.domains.amount.repository;

import kr.co.pennyway.domain.domains.amount.domain.TargetAmount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetAmountRepository extends JpaRepository<TargetAmount, Long> {
}
