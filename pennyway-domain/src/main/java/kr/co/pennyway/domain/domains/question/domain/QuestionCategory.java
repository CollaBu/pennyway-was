package kr.co.pennyway.domain.domains.question.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum QuestionCategory {
    UTILIZATION("이용 관련"),
    BUG_REPORT("오류 신고"),
    SUGGESTION("서비스 제안"),
    ETC("기타");

    private final String title;
}
