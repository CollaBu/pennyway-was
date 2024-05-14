package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Mapper
@RequiredArgsConstructor
public class TargetAmountMapper {
    public static TargetAmountDto.WithTotalSpendingRes toWithTotalSpendingRes(Optional<TargetAmount> targetAmount, Optional<TotalSpendingAmount> totalSpending, LocalDate date) {
        Integer totalSpendingAmount = totalSpending.map(TotalSpendingAmount::totalSpending).orElse(0);

        return createWithTotalSpendingRes(targetAmount.orElse(null), totalSpendingAmount, date);
    }

    public static List<TargetAmountDto.WithTotalSpendingRes> toWithTotalSpendingsRes(List<TargetAmount> targetAmounts, List<TotalSpendingAmount> totalSpendings, LocalDate startAt, LocalDate endAt) {
        int monthLength = (endAt.getYear() - startAt.getYear()) * 12 + endAt.getMonthValue() - startAt.getMonthValue();

        Map<YearMonth, TargetAmount> targetAmountsByDates = targetAmounts.stream()
                .collect(Collectors.toMap(
                        targetAmount -> YearMonth.of(targetAmount.getCreatedAt().getYear(), targetAmount.getCreatedAt().getMonthValue()),
                        targetAmount -> targetAmount));

        Map<YearMonth, Integer> totalSpendingAmounts = totalSpendings.stream()
                .collect(Collectors.toMap(
                        totalSpendingAmount -> YearMonth.of(totalSpendingAmount.year(), totalSpendingAmount.month()),
                        TotalSpendingAmount::totalSpending));

        log.info("targetAmountsByDates : {}, totalSpendingAmounts : {}", targetAmountsByDates, totalSpendingAmounts);

        List<TargetAmountDto.WithTotalSpendingRes> withTotalSpendingResList = new ArrayList<>(monthLength + 1);

        for (int i = 0; i < monthLength + 1; i++) {
            LocalDate date = startAt.plusMonths(i);
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());

            TargetAmount targetAmount = targetAmountsByDates.getOrDefault(yearMonth, null);
            Integer totalSpending = totalSpendingAmounts.getOrDefault(yearMonth, 0);

            withTotalSpendingResList.add(createWithTotalSpendingRes(targetAmount, totalSpending, date));
        }

        return withTotalSpendingResList.stream()
                .sorted(Comparator.comparing(TargetAmountDto.WithTotalSpendingRes::year).reversed()
                        .thenComparing(Comparator.comparing(TargetAmountDto.WithTotalSpendingRes::month).reversed()))
                .toList();
    }

    private static TargetAmountDto.WithTotalSpendingRes createWithTotalSpendingRes(TargetAmount targetAmount, Integer totalSpendingAmount, LocalDate date) {
        TargetAmountDto.TargetAmountInfo targetAmountInfo = TargetAmountDto.TargetAmountInfo.from(targetAmount);
        log.info("targetAmountInfo : {}, totalSpendingAmount : {}", targetAmountInfo, totalSpendingAmount);

        return TargetAmountDto.WithTotalSpendingRes.builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .targetAmount(targetAmountInfo)
                .totalSpending(totalSpendingAmount)
                .diffAmount((targetAmountInfo.amount() == -1) ? 0 : totalSpendingAmount - targetAmountInfo.amount())
                .build();
    }
}
