package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import kr.co.pennyway.domain.domains.target.domain.TargetAmount;
import lombok.Builder;

import java.time.LocalDate;

public class TargetAmountDto {
    @Schema(title = "목표 금액 등록/수정 요청 파라미터")
    public record UpdateParamReq(
            @Schema(description = "등록하려는 목표 금액 날짜 (당일)", example = "2024-05-08", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "date 값은 필수입니다.")
            @JsonSerialize(using = LocalDateSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
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

    @Builder
    @Schema(title = "목표 금액 및 총 지출 금액 조회 응답")
    public record WithTotalSpendingRes(
            @Schema(description = "조회 년도", example = "2024", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "year 값은 필수입니다.")
            Integer year,
            @Schema(description = "조회 월", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "month 값은 필수입니다.")
            Integer month,
            @Schema(description = "목표 금액", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "targetAmount 값은 필수입니다.")
            TargetAmountInfo targetAmount,
            @Schema(description = "총 지출 금액", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "totalSpending 값은 필수입니다.")
            Integer totalSpending,
            @Schema(description = "목표 금액과 총 지출 금액의 차액(총 치줄 금액 - 목표 금액). 양수면 초과, 음수면 절약", example = "-50000", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer diffAmount
    ) {
    }

    public record TargetAmountInfo(
            @Schema(description = "목표 금액 pk. 실제 저장된 데이터가 아니라면 -1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "id 값은 필수입니다.")
            Long id,
            @Schema(description = "목표 금액. -1이면 설정한 목표 금액이 존재하지 않음을 의미한다.", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "amount 값은 필수입니다.")
            Integer amount
    ) {
        public TargetAmountInfo {
            if (id == null) {
                id = -1L;
            }

            if (amount == null) {
                amount = -1;
            }
        }

        public static TargetAmountInfo from(TargetAmount targetAmount) {
            if (targetAmount == null) {
                return new TargetAmountInfo(-1L, -1);
            }
            return new TargetAmountInfo(targetAmount.getId(), targetAmount.getAmount());
        }
    }
}
