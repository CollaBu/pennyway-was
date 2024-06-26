package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Mapper
public class TargetAmountMapper {
    /**
     * TargetAmount와 TotalSpendingAmount를 이용하여 WithTotalSpendingRes를 생성한다.
     *
     * @param targetAmount  {@link TargetAmount} : 값이 없을 경우 null
     * @param totalSpending {@link TotalSpendingAmount} : 값이 없을 경우 null
     */
    public static TargetAmountDto.WithTotalSpendingRes toWithTotalSpendingResponse(TargetAmount targetAmount, TotalSpendingAmount totalSpending, LocalDate date) {
        Integer totalSpendingAmount = (totalSpending != null) ? totalSpending.totalSpending() : 0;

        return createWithTotalSpendingRes(targetAmount, totalSpendingAmount, date);
    }

    /**
     * TargetAmount와 TotalSpendingAmount를 이용하여 WithTotalSpendingRes 리스트를 생성한다. <br/>
     * startAt부터 endAt까지의 날짜에 대한 WithTotalSpendingRes를 생성하며, 임의의 날짜에 대한 정보가 없을 경우 더미 데이터를 생성한다. <br/>
     * startAt은 목표 금액 데이터 중 가장 오래된 날짜를 기준으로 잡는다.
     *
     * @param endAt : 조회 종료 날짜. 이유가 없다면 현재 날짜이며, 클라이언트로 부터 받은 날짜를 사용한다.
     */
    public static List<TargetAmountDto.WithTotalSpendingRes> toWithTotalSpendingResponses(List<TargetAmount> targetAmounts, List<TotalSpendingAmount> totalSpendings, LocalDate endAt) {
        LocalDate startAt = getOldestDate(targetAmounts);
        int monthLength = (endAt.getYear() - startAt.getYear()) * 12 + (endAt.getMonthValue() - startAt.getMonthValue());

        Map<YearMonth, TargetAmount> targetAmountsByDates = toYearMonthMap(targetAmounts, targetAmount -> YearMonth.of(targetAmount.getCreatedAt().getYear(), targetAmount.getCreatedAt().getMonthValue()), Function.identity());
        Map<YearMonth, Integer> totalSpendingAmounts = toYearMonthMap(totalSpendings, totalSpendingAmount -> YearMonth.of(totalSpendingAmount.year(), totalSpendingAmount.month()), TotalSpendingAmount::totalSpending);

        return createWithTotalSpendingResponses(targetAmountsByDates, totalSpendingAmounts, startAt, monthLength).stream()
                .sorted(Comparator.comparing(TargetAmountDto.WithTotalSpendingRes::year).reversed()
                        .thenComparing(Comparator.comparing(TargetAmountDto.WithTotalSpendingRes::month).reversed()))
                .toList();
    }

    /**
     * 최근 목표 금액을 응답 형태로 변환한다.
     *
     * @return {@link TargetAmountDto.RecentTargetAmountRes}
     */
    public static TargetAmountDto.RecentTargetAmountRes toRecentTargetAmountResponse(Optional<TargetAmount> targetAmount) {
        if (targetAmount.isEmpty()) {
            return TargetAmountDto.RecentTargetAmountRes.notPresent();
        }

        Integer year = targetAmount.get().getCreatedAt().getYear();
        Integer month = targetAmount.get().getCreatedAt().getMonthValue();
        Integer amount = targetAmount.get().getAmount();

        return TargetAmountDto.RecentTargetAmountRes.of(year, month, amount);
    }

    private static List<TargetAmountDto.WithTotalSpendingRes> createWithTotalSpendingResponses(Map<YearMonth, TargetAmount> targetAmounts, Map<YearMonth, Integer> totalSpendings, LocalDate startAt, int monthLength) {
        List<TargetAmountDto.WithTotalSpendingRes> withTotalSpendingResponses = new ArrayList<>(monthLength + 1);

        for (int i = 0; i < monthLength + 1; i++) {
            LocalDate date = startAt.plusMonths(i);
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());

            TargetAmount targetAmount = targetAmounts.getOrDefault(yearMonth, null);
            Integer totalSpending = totalSpendings.getOrDefault(yearMonth, 0);

            withTotalSpendingResponses.add(createWithTotalSpendingRes(targetAmount, totalSpending, date));
        }

        return withTotalSpendingResponses;
    }

    private static TargetAmountDto.WithTotalSpendingRes createWithTotalSpendingRes(TargetAmount targetAmount, Integer totalSpending, LocalDate date) {
        TargetAmountDto.TargetAmountInfo targetAmountInfo = TargetAmountDto.TargetAmountInfo.from(targetAmount);

        return TargetAmountDto.WithTotalSpendingRes.builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .targetAmountDetail(targetAmountInfo)
                .totalSpending(totalSpending)
                .diffAmount((targetAmountInfo.amount() == -1) ? 0 : totalSpending - targetAmountInfo.amount())
                .build();
    }

    private static LocalDate getOldestDate(List<TargetAmount> targetAmounts) {
        LocalDate minDate = LocalDate.now();

        for (TargetAmount targetAmount : targetAmounts) {
            LocalDate date = targetAmount.getCreatedAt().toLocalDate();
            if (date.isBefore(minDate)) {
                minDate = date;
            }
        }

        return minDate;
    }

    /**
     * List를 YearMonth를 key로 하는 Map으로 변환한다.
     *
     * @param keyMapper   : YearMonth로 변환할 Function
     * @param valueMapper : Value로 변환할 Function
     */
    private static <T, U> Map<YearMonth, U> toYearMonthMap(List<T> list, Function<T, YearMonth> keyMapper, Function<T, U> valueMapper) {
        return list.stream().collect(
                Collectors.toMap(
                        keyMapper,
                        valueMapper,
                        (existing, replacement) -> existing
                )
        );
    }
}
