package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SpendingCustomCategoryRepository extends JpaRepository<SpendingCustomCategory, Long> {
    @Transactional(readOnly = true)
    boolean existsByIdAndUser_Id(Long id, Long userId);

    @Transactional(readOnly = true)
    List<SpendingCustomCategory> findAllByUser_Id(Long userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE SpendingCustomCategory s SET s.deletedAt = NOW() WHERE s.user.id = :userId")
    void deleteAllByUserIdInQuery(Long userId);
}
