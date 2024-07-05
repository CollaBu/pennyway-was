package kr.co.pennyway.domain.domains.notification.type;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.Getter;

@Getter
public enum Announcement implements LegacyCommonType {
    NOT_ANNOUNCE("0", "", ""),

    // 정기 지출 알림
    DAILY_SPENDING("1", "%s님, 3분 카레보다 빨리 끝나요!", "많은 친구들이 소비 기록에 참여하고 있어요👀"),
    MONTHLY_TARGET_AMOUNT("2", "6월의 첫 시작! 두구두구..🥁", "%s님의 이번 달 목표 소비 금액은?");

    private final String code;
    private final String title;
    private final String content;

    Announcement(String code, String title, String content) {
        this.code = code;
        this.title = title;
        this.content = content;
    }
}
