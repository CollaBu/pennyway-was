package kr.co.pennyway.domain.domains.target.repository;

import java.time.LocalDate;

public interface TargetAmountCustomRepository {
    boolean existsByUserIdThatMonth(Long userId, LocalDate date);
}
