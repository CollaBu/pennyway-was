package kr.co.pennyway.domain.domains.target.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TargetAmountRepository extends ExtendedRepository<TargetAmount, Long>, TargetAmountCustomRepository {
    @Transactional(readOnly = true)
    @Query("SELECT ta FROM TargetAmount ta WHERE ta.user.id = :userId AND YEAR(ta.createdAt) = YEAR(:date) AND MONTH(ta.createdAt) = MONTH(:date)")
    Optional<TargetAmount> findByUserIdThatMonth(Long userId, LocalDate date);

    @Transactional(readOnly = true)
    Optional<TargetAmount> findByIdAndUser_Id(Long id, Long userId);

    @Transactional(readOnly = true)
    List<TargetAmount> findByUser_Id(Long userId);
}
