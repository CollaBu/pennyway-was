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
        long totalSpendingAmount = (totalSpending != null) ? totalSpending.totalSpending() : 0L;

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

        Map<YearMonth, TargetAmount> targetAmountsByDates = toYearMonthMap(targetAmounts, ta -> YearMonth.from(ta.getCreatedAt()), Function.identity());
        Map<YearMonth, Long> totalSpendingAmounts = toYearMonthMap(totalSpendings, TotalSpendingAmount::getYearMonth, TotalSpendingAmount::totalSpending);

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
        return targetAmount.map(ta -> {
            LocalDate createdAt = ta.getCreatedAt().toLocalDate();
            return TargetAmountDto.RecentTargetAmountRes.of(createdAt.getYear(), createdAt.getMonthValue(), ta.getAmount());
        }).orElseGet(TargetAmountDto.RecentTargetAmountRes::notPresent);
    }

    private static List<TargetAmountDto.WithTotalSpendingRes> createWithTotalSpendingResponses(Map<YearMonth, TargetAmount> targetAmounts, Map<YearMonth, Long> totalSpendings, LocalDate startAt, int monthLength) {
        List<TargetAmountDto.WithTotalSpendingRes> withTotalSpendingResponses = new ArrayList<>(monthLength + 1);
        LocalDate date = startAt;

        for (int i = 0; i < monthLength + 1; i++) {
            YearMonth yearMonth = YearMonth.from(date);

            TargetAmount targetAmount = targetAmounts.get(yearMonth);
            Long totalSpending = totalSpendings.getOrDefault(yearMonth, 0L);

            withTotalSpendingResponses.add(createWithTotalSpendingRes(targetAmount, totalSpending, date));
            date = date.plusMonths(1);
        }

        return withTotalSpendingResponses;
    }

    private static TargetAmountDto.WithTotalSpendingRes createWithTotalSpendingRes(TargetAmount targetAmount, Long totalSpending, LocalDate date) {
        TargetAmountDto.TargetAmountInfo targetAmountInfo = TargetAmountDto.TargetAmountInfo.from(targetAmount);
        long diffAmount = (targetAmountInfo.amount() == -1) ? 0 : totalSpending - (long) targetAmountInfo.amount();

        return TargetAmountDto.WithTotalSpendingRes.builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .targetAmountDetail(targetAmountInfo)
                .totalSpending(totalSpending)
                .diffAmount(diffAmount)
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
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper, (existing, replacement) -> existing));
    }
}
