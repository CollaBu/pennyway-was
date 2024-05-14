package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;

import java.util.Optional;

public interface SpendingCustomRepository {
    Optional<TotalSpendingAmount> findTotalSpendingAmountByUserId(Long userId, int year, int month);
}
