package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SpendingRepository extends ExtendedRepository<Spending, Long>, SpendingCustomRepository {
    @Transactional(readOnly = true)
    boolean existsByIdAndUser_Id(Long id, Long userId);

    @Transactional(readOnly = true)
    Integer countByUserIdAndIdIn(Long userId, List<Long> spendingIds);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Spending s SET s.deletedAt = NOW() where s.id IN :spendingIds")
    void deleteAllById(List<Long> spendingIds);
}
