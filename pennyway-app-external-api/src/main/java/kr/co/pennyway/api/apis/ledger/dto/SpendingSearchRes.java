package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class SpendingSearchRes {
    @Builder
    @Schema(title = "월별 지출 내역 조회 응답")
    public record Month(
            @Schema(description = "년도", example = "2024")
            int year,
            @Schema(description = "월", example = "5")
            int month,
            @Schema(description = "월별 총 지출 금액", example = "100000")
            int monthlyTotalAmount,
            @Schema(description = "일별 지출 내역")
            List<Daily> dailySpendings
    ) {
    }

    @Builder
    @Schema(title = "일별 지출 내역 조회 응답")
    public record Daily(
            @Schema(description = "일")
            int day,
            @Schema(description = "일별 총 지출 금액")
            int dailyTotalAmount,
            @Schema(description = "개별 지출 내역")
            List<Individual> individuals
    ) {
    }

    @Builder
    @Schema(title = "개별 지출 내역 조회 응답")
    public record Individual(
            @Schema(description = "지출 ID")
            @NotNull
            Long id,
            @Schema(description = "지출 금액")
            @NotNull
            Integer amount,
            @Schema(description = "지출 카테고리 아이콘")
            @NotNull
            SpendingCategory category,
            @Schema(description = "지출 일시", example = "2024-05-09")
            @NotNull
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDateTime spendAt,
            @Schema(description = "계좌명. 없으면 빈 문자열")
            String accountName,
            @Schema(description = "메모. 없으면 빈 문자열")
            String memo
    ) {
        public Individual(Long id, Integer amount, SpendingCategory category, LocalDateTime spendAt, String accountName, String memo) {
            this.id = id;
            this.amount = amount;
            this.category = category;
            this.spendAt = spendAt;
            this.accountName = Objects.toString(accountName, "");
            this.memo = Objects.toString(memo, "");
        }
    }
}
