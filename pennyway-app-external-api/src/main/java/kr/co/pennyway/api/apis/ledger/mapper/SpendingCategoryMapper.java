package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;

import java.util.List;

@Mapper
public class SpendingCategoryMapper {
    public static SpendingCategoryDto.Res toResponse(SpendingCustomCategory category) {
        return SpendingCategoryDto.Res.from(CategoryInfo.of(category.getId(), category.getName(), category.getIcon()));
    }

    public static List<SpendingCategoryDto.Res> toResponses(List<SpendingCustomCategory> categories) {
        return categories.stream()
                .map(SpendingCategoryMapper::toResponse)
                .toList();
    }
}
