package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;

public interface SpendingCustomRepository {
    TotalSpendingAmount findTotalSpendingAmountByUserId(Long userId, int year, int month);
}
