package kr.co.pennyway.api.apis.ledger.dto;

import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class SpendingSearchRes {
    @Builder
    public record Month(
            String date,
            int monthlyTotalAmount,
            List<Daily> dailySpendings
    ) {

    }

    @Builder
    public record Daily(
            int day,
            int dailyTotalAmount,
            List<Individual> spendings
    ) {

    }

    @Builder
    public record Individual(
            Long id,
            Integer amount,
            SpendingCategory icon,
            LocalDateTime spendAt,
            String accountName,
            String memo
    ) {

    }
}
