package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.constraints.NotNull;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class SpendingSearchRes {
    @Builder
    public record Month(
            int year,
            int month,
            int monthlyTotalAmount,
            List<Daily> dailySpendings
    ) {
    }

    @Builder
    public record Daily(
            int day,
            int dailyTotalAmount,
            List<Individual> individuals
    ) {
    }

    @Builder
    public record Individual(
            @NotNull
            Long id,
            @NotNull
            Integer amount,
            @NotNull
            SpendingCategory icon,
            @NotNull
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDateTime spendAt,
            String accountName,
            String memo
    ) {
        public Individual(Long id, Integer amount, SpendingCategory icon, LocalDateTime spendAt, String accountName, String memo) {
            this.id = id;
            this.amount = amount;
            this.icon = icon;
            this.spendAt = spendAt;
            this.accountName = Objects.toString(accountName, "");
            this.memo = Objects.toString(memo, "");
        }
    }
}
