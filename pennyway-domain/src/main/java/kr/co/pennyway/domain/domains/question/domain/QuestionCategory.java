package kr.co.pennyway.domain.domains.question.domain;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum QuestionCategory implements LegacyCommonType {
    UTILIZATION("1", "이용 관련"),
    BUG_REPORT("2", "오류 신고"),
    SUGGESTION("3", "서비스 제안"),
    ETC("4", "기타");

    private final String code;
    private final String title;
}
