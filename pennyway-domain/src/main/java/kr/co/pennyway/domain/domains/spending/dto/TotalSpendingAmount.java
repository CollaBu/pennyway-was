package kr.co.pennyway.domain.domains.spending.dto;

import java.time.YearMonth;

/**
 * 사용자의 해당 년/월 총 지출 금액을 담는 DTO
 */
public record TotalSpendingAmount(
        int year,
        int month,
        long totalSpending
) {
    public TotalSpendingAmount(int year, int month, long totalSpending) {
        this.year = year;
        this.month = month;
        this.totalSpending = totalSpending;
    }

    /**
     * YearMonth 객체로 변환하는 메서드
     *
     * @return 해당 년/월을 나타내는 YearMonth 객체
     */
    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }

}
