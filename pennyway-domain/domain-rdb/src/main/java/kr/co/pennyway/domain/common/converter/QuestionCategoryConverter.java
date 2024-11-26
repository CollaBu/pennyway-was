package kr.co.pennyway.domain.common.converter;

import jakarta.persistence.Converter;
import kr.co.pennyway.domain.domains.question.domain.QuestionCategory;

@Converter
public class QuestionCategoryConverter extends AbstractLegacyEnumAttributeConverter<QuestionCategory> {
    private static final String ENUM_NAME = "문의 카테고리";

    public QuestionCategoryConverter() {
        super(QuestionCategory.class, false, ENUM_NAME);
    }
}