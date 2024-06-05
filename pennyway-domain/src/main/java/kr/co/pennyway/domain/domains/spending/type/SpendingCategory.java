package kr.co.pennyway.domain.domains.spending.type;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpendingCategory implements LegacyCommonType {
    CUSTOM("0", "사용자 정의"),
    FOOD("1", "식비"),
    TRANSPORTATION("2", "교통비"),
    BEAUTY_OR_FASHION("3", "뷰티/패션"),
    CONVENIENCE_STORE("4", "편의점/마트"),
    EDUCATION("5", "교육"),
    LIVING("6", "생활"),
    HEALTH("7", "건강"),
    HOBBY("8", "취미/여가"),
    TRAVEL("9", "여행/숙박"),
    ALCOHOL_OR_ENTERTAINMENT("10", "술/유흥"),
    MEMBERSHIP_OR_FAMILY_EVENT("11", "회비/경조사"),
    OTHER("12", "기타");

    private final String code;
    private final String type;
}
