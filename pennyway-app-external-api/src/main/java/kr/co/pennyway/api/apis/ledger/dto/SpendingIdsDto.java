package kr.co.pennyway.api.apis.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SpendingIdsDto(
        @Schema(description = "지출 내역 ID 목록")
        @NotEmpty(message = "지출 내역 ID 목록은 필수입니다.")
        List<Long> spendingIds
) {
}
