package kr.co.pennyway.api.common.query;

import io.swagger.v3.oas.annotations.media.Schema;

public enum SpendingCategoryType {
    DEFAULT("default"),
    CUSTOM("custom");

    @Schema(description = "카테고리 종류<br>default / custom", example = "default")
    private final String type;

    SpendingCategoryType(String type) {
        this.type = type;
    }
}
