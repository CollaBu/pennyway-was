package kr.co.pennyway.domain.domains.spending.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.domains.spending.domain.QSpending;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.repository.SpendingRepository;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingService {
    private final SpendingRepository spendingRepository;

    private final QUser user = QUser.user;
    private final QSpending spending = QSpending.spending;

    @Transactional
    public Spending createSpending(Spending spending) {
        return spendingRepository.save(spending);
    }

    @Transactional(readOnly = true)
    public Optional<Spending> readSpending(Long spendingId) {
        return spendingRepository.findById(spendingId);
    }

    @Transactional(readOnly = true)
    public Optional<TotalSpendingAmount> readTotalSpendingAmountByUserId(Long userId, LocalDate date) {
        return spendingRepository.findTotalSpendingAmountByUserId(userId, date.getYear(), date.getMonthValue());
    }

    @Transactional(readOnly = true)
    public Optional<List<Spending>> readSpendings(Long userId, int year, int month) {
        return spendingRepository.findByYearAndMonth(userId, year, month);
    }

    @Transactional(readOnly = true)
    public List<TotalSpendingAmount> readTotalSpendingsAmountByUserId(Long userId) {
        Predicate predicate = user.id.eq(userId);

        QueryHandler queryHandler = query -> query.leftJoin(spending).on(user.id.eq(spending.user.id))
                .groupBy(spending.spendAt.year(), spending.spendAt.month());

        Sort sort = Sort.by(Sort.Order.desc("year(spendAt)"), Sort.Order.desc("month(spendAt)"));

        Map<String, Expression<?>> bindings = new LinkedHashMap<>();
        bindings.put("year", spending.spendAt.year());
        bindings.put("month", spending.spendAt.month());
        bindings.put("totalSpending", spending.amount.sum());

        return spendingRepository.selectList(predicate, TotalSpendingAmount.class, bindings, queryHandler, sort);
    }

    @Transactional(readOnly = true)
    public boolean isExistsSpending(Long userId, Long spendingId) {
        return spendingRepository.existsByIdAndUser_Id(spendingId, userId);
    }

    @Transactional
    public void deleteSpending(Spending spending) {
        spendingRepository.delete(spending);
    }
}
