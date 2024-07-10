package kr.co.pennyway.api.apis.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.api.common.query.SpendingCategoryType;

public record SpendingMigrateDto(
        @Schema(description = "이동할 지출 카테고리 ID", example = "1")
        Long toCategoryId,
        @Schema(description = "이동할 카테고리 종류<br>default / custom", example = "default")
        SpendingCategoryType toType
) {
}
