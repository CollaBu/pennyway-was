package kr.co.pennyway.domain.domains.notification.type;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.Getter;

/**
 * 알림 종류를 정의하기 위한 타입
 *
 * <p>
 * 알림 타입은 [도메인]_[액션]_[FROM]_[TO?] 형태로 정의한다.
 * 각 알림 타입에 대한 이름, 제목, 내용 형식을 지정하며, 알림 타입에 따라 내용을 생성하는 기능을 제공한다.
 * </p>
 *
 * @author YANG JAESEO
 * @since 2024-07-04
 */
@Getter
public enum NoticeType implements LegacyCommonType {
    ANNOUNCEMENT("0", "%s", "%s"); // 공지 사항은 별도 제목을 설정하여 사용한다.

    private final String code;
    private final String title;
    private final String contentFormat;
    private final String navigablePlaceholders = "{%s_%d}";
    private final String plainTextPlaceholders = "%s";

    NoticeType(String code, String title, String contentFormat) {
        this.code = code;
        this.title = title;
        this.contentFormat = contentFormat;
    }
}
