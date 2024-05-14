package kr.co.pennyway.api.apis.ledger.service;

import com.querydsl.core.types.Predicate;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.domains.spending.domain.QSpending;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingSearchService {
    private final SpendingService spendingService;

    private final QUser user = QUser.user;
    private final QSpending spending = QSpending.spending;

    /**
     * 사용자의 해당 년/월 지출 내역을 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public List<Spending> readSpendings(Long userId, int year, int month) {
        Predicate predicate = spending.user.id.eq(userId)
                .and(spending.spendAt.year().eq(year))
                .and(spending.spendAt.month().eq(month));

        QueryHandler queryHandler = query -> query.leftJoin(user).on(spending.user.eq(user));

        Sort sort = Sort.by(Sort.Order.desc("spendAt"));

        return spendingService.readSpendings(predicate, queryHandler, sort);
    }

    @Transactional(readOnly = true)
    public Spending readSpending(Long spendingId) {
        Optional<Spending> spending = spendingService.readSpending(spendingId);

        if (!spending.isPresent()) {
            throw new SpendingErrorException(SpendingErrorCode.NOT_FOUND_SPENDING);
        }

        return spending.get();
    }
}
