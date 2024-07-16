package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import org.springframework.data.domain.Slice;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Mapper
public class SpendingMapper {
    /**
     * Slice 객체를 받아 년/월/일 별로 지출 내역을 그룹화 및 정렬화 후 {@link SpendingSearchRes.MonthSlice}로 변환하는 메서드
     */
    public static SpendingSearchRes.MonthSlice toMonthSlice(Slice<Spending> spendings) {
        List<Spending> spendingList = spendings.getContent();

        // 연도와 월별로 그룹화
        ConcurrentMap<YearMonth, List<Spending>> groupSpendingsByYearAndMonth = spendingList.stream()
                .collect(Collectors.groupingByConcurrent(spending -> YearMonth.of(spending.getSpendAt().getYear(), spending.getSpendAt().getMonthValue())));

        // 그룹화된 결과를 Month 객체로 변환하고, 년-월 순으로 역정렬
        List<SpendingSearchRes.Month> months = groupSpendingsByYearAndMonth.entrySet().stream()
                .map(entry -> toSpendingSearchResMonth(entry.getValue(), entry.getKey().getYear(), entry.getKey().getMonthValue()))
                .sorted(Comparator.comparing(SpendingSearchRes.Month::year)
                        .thenComparing(SpendingSearchRes.Month::month)
                        .reversed())
                .toList();

        return SpendingSearchRes.MonthSlice.from(months, spendings.getPageable(), spendings.getNumberOfElements(), spendings.hasNext());
    }

    /**
     * 년/월 별로 지출 내역을 그룹화 및 정렬화 후 {@link SpendingSearchRes.Month}로 변환하는 메서드
     */
    public static SpendingSearchRes.Month toSpendingSearchResMonth(List<Spending> spendings, int year, int month) {
        ConcurrentMap<Integer, List<Spending>> groupSpendingsByDay = spendings.stream().collect(Collectors.groupingByConcurrent(Spending::getDay));

        // 그룹화된 결과를 Daily 객체로 변환하고, 일(day)을 기준으로 역정렬
        List<SpendingSearchRes.Daily> dailySpendings = groupSpendingsByDay.entrySet().stream()
                .map(entry -> toSpendingSearchResDaily(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SpendingSearchRes.Daily::day).reversed())
                .toList();

        return SpendingSearchRes.Month.builder()
                .year(year)
                .month(month)
                .dailySpendings(dailySpendings)
                .build();
    }

    /**
     * 일 별로 지출 내역을 정렬 후 {@link SpendingSearchRes.Daily}로 변환하는 메서드
     */
    private static SpendingSearchRes.Daily toSpendingSearchResDaily(int day, List<Spending> spendings) {
        // 지출 내역을 id 순으로 정렬
        List<SpendingSearchRes.Individual> individuals = spendings.stream()
                .map(SpendingMapper::toSpendingSearchResIndividual)
                .sorted(Comparator.comparing(SpendingSearchRes.Individual::id))
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
    private static long calculateDailyTotalAmount(List<Spending> spendings) {
        return spendings.stream().mapToLong(Spending::getAmount).sum();
    }
}
