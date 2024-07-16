package kr.co.pennyway.api.apis.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class SpendingSearchRes {
    @Builder
    @Schema(title = "월별 지출 내역 조회 슬라이스 응답")
    public record MonthSlice(
            @Schema(description = "년/월별 지출 내역")
            List<Month> content,
            @Schema(description = "현재 페이지 번호")
            int currentPageNumber,
            @Schema(description = "페이지 크기")
            int pageSize,
            @Schema(description = "전체 요소 개수")
            int numberOfElements,
            @Schema(description = "다음 페이지 존재 여부")
            boolean hasNext
    ) {
        public static MonthSlice from(List<Month> months, Pageable pageable, int numberOfElements, boolean hasNext) {
            return new MonthSlice(months, pageable.getPageNumber(), pageable.getPageSize(), numberOfElements, hasNext);
        }
    }

    @Builder
    @Schema(title = "월별 지출 내역 조회 응답")
    public record Month(
            @Schema(description = "년도", example = "2024")
            int year,
            @Schema(description = "월", example = "5")
            int month,
            @Schema(description = "일별 지출 내역")
            List<Daily> dailySpendings
    ) {
    }

    @Builder
    @Schema(title = "일별 지출 내역 조회 응답")
    public record Daily(
            @Schema(description = "일")
            int day,
            @Schema(description = "일별 총 지출 금액")
            long dailyTotalAmount,
            @Schema(description = "개별 지출 내역")
            List<Individual> individuals
    ) {
    }

    @Builder
    @Schema(title = "개별 지출 내역 조회 응답")
    public record Individual(
            @Schema(description = "지출 ID")
            @NotNull
            Long id,
            @Schema(description = "지출 금액")
            @NotNull
            Integer amount,
            @Schema(description = "지출 카테고리 정보")
            @NotNull
            CategoryInfo category,
            @Schema(description = "지출 일시", pattern = "yyyy-MM-dd HH:mm:ss", example = "2021-08-01 00:00:00")
            @NotNull
            @JsonSerialize(using = LocalDateTimeSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime spendAt,
            @Schema(description = "계좌명. 없으면 빈 문자열", example = "카페인 수혈")
            String accountName,
            @Schema(description = "메모. 없으면 빈 문자열", example = "아메리카노 1잔")
            String memo
    ) {
        public Individual(Long id, Integer amount, CategoryInfo category, LocalDateTime spendAt, String accountName, String memo) {
            this.id = id;
            this.amount = amount;
            this.category = category;
            this.spendAt = spendAt;
            this.accountName = Objects.toString(accountName, "");
            this.memo = Objects.toString(memo, "");
        }
    }
}
