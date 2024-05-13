package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class TargetAmountDto {
    @Schema(title = "목표 금액 등록/수정 요청 파라미터")
    public record UpdateParamReq(
            @Schema(description = "등록하려는 목표 금액 날짜 (당일)", example = "2024-05-08", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "date 값은 필수입니다.")
            @JsonSerialize(using = LocalDateSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
            @PastOrPresent(message = "date 값은 과거 또는 현재 날짜여야 합니다.")
            LocalDate date,
            @Schema(description = "등록하려는 목표 금액 (0이상의 정수)", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "amount 값은 필수입니다.")
            @Min(value = 0, message = "amount 값은 0 이상이어야 합니다.")
            Integer amount
    ) {

    }

    @Schema(title = "목표 금액 조회 요청 파라미터")
    public record GetParamReq(
            @Schema(description = "조회하려는 목표 금액 날짜 (당일)", example = "2024-05-08", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "date 값은 필수입니다.")
            @JsonSerialize(using = LocalDateSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
            @PastOrPresent(message = "date 값은 과거 또는 현재 날짜여야 합니다.")
            LocalDate date
    ) {

    }

    @Schema(title = "목표 금액 조회 응답")
    public record GetResponse(
            @Schema(description = "목표 금액 날짜", example = "2024-05-08", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "date 값은 필수입니다.")
            @JsonSerialize(using = LocalDateSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
            @PastOrPresent(message = "date 값은 과거 또는 현재 날짜여야 합니다.")
            LocalDate date,
            @Schema(description = "목표 금액", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
            int targetAmount,
            @Schema(description = "총 지출 금액", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
            int totalSpending,
            @Schema(description = "목표 금액과 총 지출 금액의 차액(총 치줄 금액 - 목표 금액). 양수면 초과, 음수면 절약", example = "-50000", requiredMode = Schema.RequiredMode.REQUIRED)
            int diffAmount
    ) {

    }
}
