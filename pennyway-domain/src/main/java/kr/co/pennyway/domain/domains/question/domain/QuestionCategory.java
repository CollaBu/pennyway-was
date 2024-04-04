package kr.co.pennyway.domain.domains.question.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 문의 요청 DTO
 * <br/>
 * 문의 요청시 사용됩니다.
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum QuestionCategory {
    UTILIZATION("이용 관련"),
    BUG_REPORT("오류 신고"),
    SUGGESTION("서비스 제안"),
    ETC("기타");

    private final String title;
}
