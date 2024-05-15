package kr.co.pennyway.domain.domains.spending.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.spending.domain.Spending;

public interface SpendingRepository extends ExtendedRepository<Spending, Long>, SpendingCustomRepository {
}
