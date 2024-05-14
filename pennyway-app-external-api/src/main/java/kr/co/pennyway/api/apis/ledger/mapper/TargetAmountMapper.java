package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Mapper
@RequiredArgsConstructor
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
     * startAt부터 endAt까지의 날짜에 대한 WithTotalSpendingRes를 생성하며, 임의의 날짜에 대한 정보가 없을 경우 더미 데이터를 생성한다.
     *
     * @param startAt : 조회 시작 날짜. 이유가 없다면 사용자 생성 날짜를 사용한다.
     * @param endAt   : 조회 종료 날짜. 이유가 없다면 현재 날짜이며, 클라이언트로 부터 받은 날짜를 사용한다.
     */
    public static List<TargetAmountDto.WithTotalSpendingRes> toWithTotalSpendingResponses(List<TargetAmount> targetAmounts, List<TotalSpendingAmount> totalSpendings, LocalDate startAt, LocalDate endAt) {
        int monthLength = (endAt.getYear() - startAt.getYear()) * 12 + (endAt.getMonthValue() - startAt.getMonthValue());

        Map<YearMonth, TargetAmount> targetAmountsByDates = toYearMonthMap(targetAmounts, targetAmount -> YearMonth.of(targetAmount.getCreatedAt().getYear(), targetAmount.getCreatedAt().getMonthValue()), Function.identity());
        Map<YearMonth, Integer> totalSpendingAmounts = toYearMonthMap(totalSpendings, totalSpendingAmount -> YearMonth.of(totalSpendingAmount.year(), totalSpendingAmount.month()), TotalSpendingAmount::totalSpending);

        return createWithTotalSpendingResponses(targetAmountsByDates, totalSpendingAmounts, startAt, monthLength).stream()
                .sorted(Comparator.comparing(TargetAmountDto.WithTotalSpendingRes::year).reversed()
                        .thenComparing(Comparator.comparing(TargetAmountDto.WithTotalSpendingRes::month).reversed()))
                .toList();
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
                .targetAmount(targetAmountInfo)
                .totalSpending(totalSpending)
                .diffAmount((targetAmountInfo.amount() == -1) ? 0 : totalSpending - targetAmountInfo.amount())
                .build();
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
