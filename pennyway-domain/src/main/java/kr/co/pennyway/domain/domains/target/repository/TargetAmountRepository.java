package kr.co.pennyway.domain.domains.target.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface TargetAmountRepository extends ExtendedRepository<TargetAmount, Long> {
    @Query("SELECT ta FROM TargetAmount ta WHERE ta.user.id = :userId AND YEAR(ta.createdAt) = YEAR(:date) AND MONTH(ta.createdAt) = MONTH(:date)")
    Optional<TargetAmount> findByUserIdAndDateThatMonth(Long userId, LocalDate date);
}
