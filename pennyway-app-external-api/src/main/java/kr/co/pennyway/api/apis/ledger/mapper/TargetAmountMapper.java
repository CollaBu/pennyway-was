package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
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
        // startAt부터 endAt까지의 월 길이 계산
        int monthLength = (endAt.getYear() - startAt.getYear()) * 12 + endAt.getMonthValue() - startAt.getMonthValue() + 1;
        log.info("monthLength : {}", monthLength);

        // TargetAmount의 createdAt을 이용하여 startAt부터 endAt까지의 TargetAmount 리스트 생성. 없으면 null (day는 무시)
        List<TargetAmount> targetAmountsByDates = new ArrayList<>();
        for (int i = 0; i < monthLength; i++) {
            LocalDate date = startAt.plusMonths(i);
            targetAmountsByDates.add(targetAmounts.stream()
                    .filter(targetAmount -> targetAmount.getCreatedAt().getYear() == date.getYear() && targetAmount.getCreatedAt().getMonth() == date.getMonth())
                    .findFirst()
                    .orElse(null));
        }

        // TotalSpendingAmount의 year과 month를 이용하여 startAt부터 endAt까지의 Integer 리스트 생성. 없으면 0 (day는 무시)
        List<Integer> totalSpendingAmounts = new ArrayList<>();
        for (int i = 0; i < monthLength; i++) {
            LocalDate date = startAt.plusMonths(i);
            totalSpendingAmounts.add(totalSpendings.stream()
                    .filter(totalSpendingAmount -> totalSpendingAmount.year() == date.getYear() && totalSpendingAmount.month() == date.getMonthValue())
                    .findFirst()
                    .map(TotalSpendingAmount::totalSpending)
                    .orElse(0));
        }

        // startAt부터 endAt까지의 TargetAmount와 TotalSpendingAmount를 이용하여 WithTotalSpendingRes 리스트 생성
        List<TargetAmountDto.WithTotalSpendingRes> withTotalSpendingResList = new ArrayList<>();
        for (int i = 0; i < monthLength; i++) {
            LocalDate date = startAt.plusMonths(i);
            withTotalSpendingResList.add(createWithTotalSpendingRes(targetAmountsByDates.get(i), totalSpendingAmounts.get(i), date));
        }

        // WithTotalSpendingRes 리스트를 year, month 역순으로 정렬하여 반환
        return withTotalSpendingResList;
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
