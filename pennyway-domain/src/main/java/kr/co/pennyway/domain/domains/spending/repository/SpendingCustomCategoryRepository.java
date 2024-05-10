package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SpendingCustomCategoryRepository extends JpaRepository<SpendingCustomCategory, Long> {
    @Transactional(readOnly = true)
    boolean existsByIdAndUser_Id(Long id, Long userId);
}
