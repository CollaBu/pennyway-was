package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Mapper
@RequiredArgsConstructor
public class TargetAmountMapper {
    public static TargetAmountDto.WithTotalSpendingRes toWithTotalSpendingRes(Optional<TargetAmount> targetAmount, Optional<TotalSpendingAmount> totalSpending, LocalDate date) {
        Integer totalSpendingAmount = totalSpending.map(TotalSpendingAmount::totalSpending).orElse(0);

        return createWithTotalSpendingRes(targetAmount.orElse(null), totalSpendingAmount, date);
    }

    public static List<TargetAmountDto.WithTotalSpendingRes> toWithTotalSpendingsRes(List<TargetAmount> targetAmounts, List<TotalSpendingAmount> totalSpendings, LocalDate startAt, LocalDate endAt) {
        // startAt부터 endAt까지의 날짜 리스트 생성
        List<LocalDate> dates = startAt.datesUntil(endAt.plusDays(1)).toList();
        log.info("dates : {}", dates);

        // TargetAmount의 createdAt을 이용하여 startAt부터 endAt까지의 TargetAmount 리스트 생성. 없으면 null
        List<TargetAmount> targetAmountsByDates = dates.stream()
                .map(date -> targetAmounts.stream()
                        .filter(targetAmount -> targetAmount.getCreatedAt().toLocalDate().isEqual(date))
                        .findFirst()
                        .orElse(null))
                .toList();
        log.info("targetAmountsByDates : {}", targetAmountsByDates);

        // TotalSpendingAmount의 year과 month를 이용하여 startAt부터 endAt까지의 Integer 리스트 생성. 없으면 0
        List<Integer> totalSpendingAmounts = dates.stream()
                .map(date -> totalSpendings.stream()
                        .filter(totalSpending -> totalSpending.year().equals(date.getYear()) && totalSpending.month().equals(date.getMonthValue()))
                        .findFirst()
                        .map(TotalSpendingAmount::totalSpending)
                        .orElse(0))
                .toList();
        log.info("totalSpendingsByDates : {}", totalSpendingAmounts);

        // startAt부터 endAt까지의 TargetAmount와 TotalSpendingAmount를 이용하여 WithTotalSpendingRes 리스트 생성
        return dates.stream()
                .map(date -> {
                    TargetAmount targetAmount = targetAmountsByDates.get(dates.indexOf(date));
                    Integer totalSpending = totalSpendingAmounts.get(dates.indexOf(date));
                    return createWithTotalSpendingRes(targetAmount, totalSpending, date);
                })
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
                .diffAmount(totalSpendingAmount - targetAmountInfo.amount())
                .build();
    }
}
