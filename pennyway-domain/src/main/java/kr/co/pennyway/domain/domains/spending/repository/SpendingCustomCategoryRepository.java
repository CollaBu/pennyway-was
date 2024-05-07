package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendingCustomCategoryRepository extends JpaRepository<SpendingCustomCategory, Long> {
}
