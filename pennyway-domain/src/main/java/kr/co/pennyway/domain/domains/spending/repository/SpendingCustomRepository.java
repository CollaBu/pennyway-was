package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;

import java.util.List;
import java.util.Optional;

public interface SpendingCustomRepository {
    Optional<TotalSpendingAmount> findTotalSpendingAmountByUserId(Long userId, int year, int month);

    List<Spending> findByYearAndMonth(Long userId, int year, int month);

    void deleteByIds(List<Long> spendingIds);
}
