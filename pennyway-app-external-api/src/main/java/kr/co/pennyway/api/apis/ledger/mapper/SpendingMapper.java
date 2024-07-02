package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Mapper
public class SpendingMapper {
    public static List<SpendingSearchRes.Month> toSpendingByCategory(Slice<Spending> spendings) {
        List<Spending> spendingList = spendings.getContent();

        ConcurrentMap<Integer, List<Spending>> groupSpendingsByYearAndMonth = spendingList.stream()
                .collect(Collectors.groupingByConcurrent(
                        spending -> spending.getSpendAt().getYear() * 100 + spending.getSpendAt().getMonthValue()
                ));

        return groupSpendingsByYearAndMonth.entrySet().stream()
                .map(entry -> {
                    int year = entry.getKey() / 100;
                    int month = entry.getKey() % 100;
                    return toSpendingSearchResMonth(entry.getValue(), year, month);
                })
                .sorted((a, b) -> {
                    if (a.year() == b.year()) {
                        return b.month() - a.month();
                    }
                    return b.year() - a.year();
                })
                .toList();
    }

    public static SpendingSearchRes.Month toSpendingSearchResMonth(List<Spending> spendings, int year, int month) {
        ConcurrentMap<Integer, List<Spending>> groupSpendingsByDay = spendings.stream().collect(Collectors.groupingByConcurrent(Spending::getDay));

        List<SpendingSearchRes.Daily> dailySpendings = groupSpendingsByDay.entrySet().stream()
                .map(entry -> toSpendingSearchResDaily(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.day() - a.day())
                .toList();

        return SpendingSearchRes.Month.builder()
                .year(year)
                .month(month)
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

    public static SpendingSearchRes.Individual toSpendingSearchResIndividual(Spending spending) {
        return SpendingSearchRes.Individual.builder()
                .id(spending.getId())
                .amount(spending.getAmount())
                .category(spending.getCategory())
                .spendAt(spending.getSpendAt())
                .accountName(spending.getAccountName())
                .memo(spending.getMemo())
                .build();
    }

    /**
     * 하루 지출 내역의 총 금액을 계산하는 메서드
     */
    private static int calculateDailyTotalAmount(List<Spending> spendings) {
        return spendings.stream().mapToInt(Spending::getAmount).sum();
    }
}
