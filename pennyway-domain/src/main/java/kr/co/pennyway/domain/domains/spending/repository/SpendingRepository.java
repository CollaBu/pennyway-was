package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import org.springframework.transaction.annotation.Transactional;

public interface SpendingRepository extends ExtendedRepository<Spending, Long>, SpendingCustomRepository {
    @Transactional(readOnly = true)
    boolean existsByIdAndUser_Id(Long id, Long userId);

    @Transactional(readOnly = true)
    int countByUser_IdAndSpendingCustomCategory_Id(Long userId, Long categoryId);
    
    @Transactional(readOnly = true)
    int countByUser_IdAndCategory(Long userId, SpendingCategory spendingCategory);
}
