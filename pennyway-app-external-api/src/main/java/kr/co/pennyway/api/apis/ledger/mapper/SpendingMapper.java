package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.domain.Spending;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Mapper
public class SpendingMapper {
    public static SpendingSearchRes.Month toSpendingSearchResMonth(List<Spending> spendings, int year, int month) {
        ConcurrentMap<Integer, List<Spending>> groupSpendingsByDay = spendings.stream().collect(Collectors.groupingByConcurrent(Spending::getDay));

        List<SpendingSearchRes.Daily> dailySpendings = groupSpendingsByDay.entrySet().stream()
                .map(entry -> toSpendingSearchResDaily(entry.getKey(), entry.getValue()))
                .toList();

        return SpendingSearchRes.Month.builder()
                .year(year)
                .month(month)
                .monthlyTotalAmount(calculateMonthlyTotalAmount(groupSpendingsByDay))
                .dailySpendings(dailySpendings)
                .build();
    }

    private static SpendingSearchRes.Daily toSpendingSearchResDaily(int day, List<Spending> spendings) {
        List<SpendingSearchRes.Individual> individuals = spendings.stream()
                .map(SpendingMapper::toSpendingSearchResIndividual)
                .toList();

        return SpendingSearchRes.Daily.builder()
                .day(day)
                .dailyTotalAmount(calculateDailyTotalAmount(spendings))
                .individuals(individuals)
                .build();
    }

    private static SpendingSearchRes.Individual toSpendingSearchResIndividual(Spending spending) {
        return SpendingSearchRes.Individual.builder()
                .id(spending.getId())
                .amount(spending.getAmount())
                .category(spending.getCategory().icon())
                .spendAt(spending.getSpendAt())
                .accountName(spending.getAccountName())
                .memo(spending.getMemo())
                .build();
    }

    /**
     * 월별 지출 내역의 총 금액을 계산하는 메서드
     */
    private static int calculateMonthlyTotalAmount(ConcurrentMap<Integer, List<Spending>> spendings) {
        return spendings.values().stream().flatMap(List::stream).mapToInt(Spending::getAmount).sum();
    }

    /**
     * 하루 지출 내역의 총 금액을 계산하는 메서드
     */
    private static int calculateDailyTotalAmount(List<Spending> spendings) {
        return spendings.stream().mapToInt(Spending::getAmount).sum();
    }
}
