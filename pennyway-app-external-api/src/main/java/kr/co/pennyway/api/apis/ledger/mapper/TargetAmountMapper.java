package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
@RequiredArgsConstructor
public class TargetAmountMapper {
    public static TargetAmountDto.WithTotalSpendingRes toWithTotalSpendingRes(Optional<TargetAmount> targetAmount, Optional<TotalSpendingAmount> totalSpending, LocalDate date) {
        TargetAmountDto.TargetAmountInfo targetAmountInfo = TargetAmountDto.TargetAmountInfo.from(targetAmount.orElse(null));
        Integer totalSpendingAmount = totalSpending.map(TotalSpendingAmount::totalSpending).orElse(0);

        return TargetAmountDto.WithTotalSpendingRes.builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .targetAmount(targetAmountInfo)
                .totalSpending(totalSpendingAmount)
                .diffAmount(totalSpendingAmount - targetAmountInfo.amount())
                .build();
    }
}
