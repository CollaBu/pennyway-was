package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    @Schema(title = "목표 금액의 amount 유효성 검사를 위한 요청 파라미터", hidden = true)
    public record AmountParam(
            @Schema(description = "등록하려는 목표 금액 (0이상의 정수)", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
            @Min(value = 0, message = "amount 값은 0 이상이어야 합니다.")
            int amount
    ) {

    }

    @Schema(title = "목표 금액의 date 유효성 검사를 위한 요청 파라미터", hidden = true)
    public record DateParam(
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
            int year,
            @Schema(description = "조회 월", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
            int month,
            @Schema(description = "목표 금액", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "targetAmountDetail 값은 필수입니다.")
            TargetAmountInfo targetAmountDetail,
            @Schema(description = "총 지출 금액", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
            long totalSpending,
            @Schema(description = "목표 금액과 총 지출 금액의 차액(총 치줄 금액 - 목표 금액). 양수면 초과, 음수면 절약", example = "-50000", requiredMode = Schema.RequiredMode.REQUIRED)
            long diffAmount
    ) {
    }

    @Schema(title = "목표 금액 상세 정보")
    public record TargetAmountInfo(
            @Schema(description = "목표 금액 pk. 실제 저장된 데이터가 아니라면 -1", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            long id,
            @Schema(description = "목표 금액. -1이면 설정한 목표 금액이 존재하지 않음을 의미한다.", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
            int amount,
            @Schema(description = "사용자 확인 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
            boolean isRead
    ) {
        /**
         * {@link TargetAmount} -> {@link TargetAmountInfo} 변환하는 메서드 <br/>
         * 만약, 인자로 들어온 값이 null이라면 모든 값을 -1로 초기화한 더미 데이터를 반환한다.
         */
        public static TargetAmountInfo from(TargetAmount targetAmount) {
            if (targetAmount == null) {
                return new TargetAmountInfo(-1L, -1, false);
            }
            return new TargetAmountInfo(targetAmount.getId(), targetAmount.getAmount(), targetAmount.isRead());
        }
    }

    @Schema(title = "가장 최근에 입력한 목표 금액 정보")
    public record RecentTargetAmountRes(
            @Schema(description = "최근 목표 금액 존재 여부로써 데이터가 존재하지 않으면 false, 존재하면 true", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
            boolean isPresent,
            @Schema(description = "최근 목표 금액 년도 정보. isPresent가 false인 경우 필드가 존재하지 않는다.", example = "2024", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            int year,
            @Schema(description = "최근 목표 금액 월 정보. isPresent가 false인 경우 필드가 존재하지 않는다.", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            int month,
            @Schema(description = "최근 목표 금액 정보. isPresent가 false인 경우 필드가 존재하지 않는다.", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            int amount
    ) {
        public RecentTargetAmountRes {
            if (!isPresent) {
                assert year == 0;
                assert month == 0;
                assert amount == 0;
            }
        }

        public static RecentTargetAmountRes notPresent() {
            return new RecentTargetAmountRes(false, 0, 0, 0);
        }

        public static RecentTargetAmountRes of(int year, int month, int amount) {
            return (amount == -1) ? new RecentTargetAmountRes(false, 0, 0, 0) : new RecentTargetAmountRes(true, year, month, amount);
        }
    }
}
