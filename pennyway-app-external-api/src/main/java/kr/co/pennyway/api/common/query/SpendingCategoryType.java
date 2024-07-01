package kr.co.pennyway.api.common.query;

public enum SpendingCategoryType {
    DEFAULT("default"),
    CUSTOM("custom");

    private final String type;

    SpendingCategoryType(String type) {
        this.type = type;
    }
}
