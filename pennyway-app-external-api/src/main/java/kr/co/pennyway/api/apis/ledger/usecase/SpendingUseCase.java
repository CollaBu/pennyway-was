package kr.co.pennyway.api.apis.ledger.usecase;

import com.querydsl.core.types.Predicate;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.repository.QueryHandler;
import kr.co.pennyway.domain.domains.spending.domain.QSpending;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SpendingUseCase {
    private SpendingService spendingService;

    @Transactional(readOnly = true)
    public SpendingSearchRes.Month getSpendingsAtYearAndMonth(Long userId, int year, int month) {
        // 사용자의 해당 년/월 지출 내역을 조회.
        QUser user = QUser.user;
        QSpending spending = QSpending.spending;

        Predicate predicate = spending.user.id.eq(userId)
                .and(spending.spendAt.year().eq(year))
                .and(spending.spendAt.month().eq(month));
        QueryHandler queryHandler = query -> query.leftJoin(user).on(spending.user.eq(user));
        Sort sort = Sort.by(Sort.Order.desc("spendAt"));
        List<Spending> spendings = spendingService.readSpendings(predicate, queryHandler, sort);

        // 일 별로 지출 내역을 묶는 알고리즘 구현 (각 day별 지출 합계 계산)
        ConcurrentMap<Integer, List<Spending>> spendingMap = spendings.stream().collect(Collectors.groupingByConcurrent(Spending::getDay));

        // 일 별 지출 내역을 조회하여 일 별 지출 합계를 계산하여 SpendingSearchRes.Daily에 저장
        int monthlySum = spendingMap.values().stream().flatMap(List::stream).mapToInt(Spending::getAmount).sum();
        SpendingSearchRes.Month res = SpendingSearchRes.Month.builder()
                .date(year + "-" + month)
                .dailySpendings(
                        spendingMap.entrySet().stream()
                                .map(entry -> {
                                    int sum = 0;
                                    int day = entry.getKey();
                                    List<Spending> spendingList = entry.getValue();
                                    int dailySum = spendingList.stream().mapToInt(Spending::getAmount).sum();
                                    sum += dailySum;

                                    return SpendingSearchRes.Daily.builder()
                                            .day(day)
                                            .dailyTotalAmount(sum)
                                            .spendings(spendingList.stream()
                                                    .map(s -> SpendingSearchRes.Individual.builder()
                                                            .id(s.getId())
                                                            .amount(s.getAmount())
                                                            .icon(s.getCategory())
                                                            .spendAt(s.getSpendAt())
                                                            .accountName(s.getAccountName())
                                                            .memo(s.getMemo())
                                                            .build())
                                                    .collect(Collectors.toList()))
                                            .build();
                                })
                                .collect(Collectors.toList())
                )
                .monthlyTotalAmount(monthlySum)
                .build();

        return res;
    }
}