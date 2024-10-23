package kr.co.pennyway.api.common.response;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

public record SliceResponseTemplate<E>(
        List<E> content,
        int currentPageNumber,
        int pageSize,
        int numberOfElements,
        boolean hasNext
) {
    public static <E> SliceResponseTemplate<E> of(@NonNull List<E> content, @NonNull Pageable pageable, int numberOfElements, boolean hasNext) {
        return new SliceResponseTemplate<>(content, pageable.getPageNumber(), pageable.getPageSize(), numberOfElements, hasNext);
    }
}
