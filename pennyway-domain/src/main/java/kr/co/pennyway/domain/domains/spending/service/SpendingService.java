package kr.co.pennyway.domain.domains.spending.service;

import com.querydsl.core.types.Predicate;
import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.repository.SpendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingService {
    private final SpendingRepository spendingRepository;

    @Transactional
    public Spending createSpending(Spending spending) {
        return spendingRepository.save(spending);
    }

    @Transactional(readOnly = true)
    public List<Spending> readSpendings(Predicate predicate, QueryHandler queryHandler, Sort sort) {
        return spendingRepository.findList(predicate, queryHandler, sort);
    }

    @Transactional(readOnly = true)
    public Optional<Spending> readSpending(Long spendingId) {
        return spendingRepository.findById(spendingId);
    }

    @Transactional(readOnly = true)
    public boolean isExistsSpending(Long userId, Long spendingId) {
        return spendingRepository.existsByIdAndUser_Id(spendingId, userId);
    }

}
