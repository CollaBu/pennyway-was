package kr.co.pennyway.domain.domains.target.repository;

import kr.co.pennyway.domain.domains.target.domain.TargetAmount;

import java.time.LocalDate;
import java.util.Optional;

public interface TargetAmountCustomRepository {
    Optional<TargetAmount> findRecentOneByUserId(Long userId);

    boolean existsByUserIdThatMonth(Long userId, LocalDate date);
}
