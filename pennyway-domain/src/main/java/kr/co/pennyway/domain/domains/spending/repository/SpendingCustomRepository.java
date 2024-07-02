package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface SpendingCustomRepository {
    Optional<TotalSpendingAmount> findTotalSpendingAmountByUserId(Long userId, int year, int month);

    List<Spending> findByYearAndMonth(Long userId, int year, int month);

    Slice<Spending> findAllByCustomCategoryId(Long userId, Long categoryId, Pageable pageable);

    Slice<Spending> findAllByCategory(Long userId, SpendingCategory spendingCategory, Pageable pageable);
}
