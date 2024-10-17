package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;

import java.time.LocalDate;

@Schema(title = "지출 내역 추가 요청")
public record SpendingReq(
        @Schema(description = "지출 금액. int 범위 최대값까지 허용", example = "10000")
        @Min(value = 1, message = "지출 금액은 1 이상이어야 합니다.")
        int amount,
        @Schema(description = "지출 카테고리 ID. 사용자가 정의한 카테고리가 아닌 경우 -1. icon이 OTHER이면서 categoryId가 -1일 수는 없다.", example = "-1")
        @NotNull(message = "지출 카테고리 ID는 필수입니다.")
        @Min(value = -1, message = "지출 카테고리 ID는 -1 이상이어야 합니다.")
        Long categoryId,
        @Schema(description = "지출 카테고리 아이콘", example = "FOOD")
        @NotNull(message = "지출 카테고리 아이콘은 필수입니다.")
        SpendingCategory icon,
        @Schema(description = "지출 일자", example = "2021-08-01")
        @NotNull(message = "지출 일자는 필수입니다.")
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd")
        @PastOrPresent(message = "지출 일자는 과거 또는 현재여야 합니다.")
        LocalDate spendAt,
        @Schema(description = "소비처", example = "카페인 수혈")
        @Size(max = 20, message = "소비처는 null 혹은 20자 이하로 입력해야 합니다.")
        String accountName,
        @Schema(description = "메모", example = "아메리카노 1잔")
        @Size(max = 100, message = "메모는 null 혹은 100자 이하로 입력해야 합니다.")
        String memo
) {
    /**
     * 서비스에서 제공하는 지출 카테고리를 사용하는 지출 내역으로 변환
     */
    public Spending toEntity(User user) {
        return Spending.builder()
                .amount(amount)
                .category(icon)
                .spendAt(spendAt.atStartOfDay())
                .accountName(accountName)
                .memo(memo)
                .user(user)
                .build();
    }

    /**
     * 사용자가 정의한 지출 카테고리를 사용하는 지출 내역으로 변환
     */
    public Spending toEntity(User user, SpendingCustomCategory spendingCustomCategory) {
        return Spending.builder()
                .amount(amount)
                .category(icon)
                .spendAt(spendAt.atStartOfDay())
                .accountName(accountName)
                .memo(memo)
                .user(user)
                .spendingCustomCategory(spendingCustomCategory)
                .build();
    }

    @Schema(hidden = true)
    @JsonIgnore
    public boolean isCustomCategory() {
        return !categoryId.equals(-1L);
    }
}
