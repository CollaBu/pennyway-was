package kr.co.pennyway.api.apis.ledger.mapper;

import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;

@Mapper
public class SpendingCategoryMapper {
    public static SpendingCategoryDto.Res toRes(SpendingCustomCategory category) {
        return SpendingCategoryDto.Res.from(CategoryInfo.of(category.getId(), category.getName(), category.getIcon()));
    }
}
