package kr.co.pennyway.domain.domains.spending.dto;

/**
 * 사용자의 해당 년/월 총 지출 금액을 담는 DTO
 */
public record TotalSpendingAmount(
        Integer year,
        Integer month,
        Integer totalSpending
) {
    public TotalSpendingAmount(Integer year, Integer month, Integer totalSpending) {
        this.year = year;
        this.month = month;
        this.totalSpending = totalSpending;
    }
}
