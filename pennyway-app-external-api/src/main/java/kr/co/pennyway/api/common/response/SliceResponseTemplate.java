package kr.co.pennyway.api.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

@Schema(description = "페이징된 무한 스크롤 응답")
public record SliceResponseTemplate<E>(
        @Schema(description = "응답 컨텐츠 내용")
        List<E> content,
        @Schema(description = "현재 페이지 번호")
        int currentPageNumber,
        @Schema(description = "페이지 크기")
        int pageSize,
        @Schema(description = "전체 요소 개수")
        int numberOfElements,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static <E> SliceResponseTemplate<E> of(@NonNull List<E> content, @NonNull Pageable pageable, int numberOfElements, boolean hasNext) {
        return new SliceResponseTemplate<>(content, pageable.getPageNumber(), pageable.getPageSize(), numberOfElements, hasNext);
    }
}
