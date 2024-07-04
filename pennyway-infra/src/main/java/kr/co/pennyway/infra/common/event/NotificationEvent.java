package kr.co.pennyway.infra.common.event;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * FCM 푸시 알림에 필요한 정보를 담은 Event 클래스
 * <p>
 * 제목, 내용, 디바이스 토큰 리스트를 필드로 갖는다.
 */
@Getter
public class NotificationEvent {
    private final String title;
    private final String content;
    private final List<String> deviceTokens;

    private NotificationEvent(String title, String content, List<String> deviceTokens) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content) || deviceTokens == null) {
            throw new IllegalArgumentException("NotificationEvent는 반드시 null 혹은 공백 문자가 아닌 title, content와 null이 아닌 deviceTokens를 가져야 합니다.");
        }

        this.title = title;
        this.content = content;
        this.deviceTokens = deviceTokens;
    }

    public static NotificationEvent of(String title, String content, List<String> deviceTokens) {
        return new NotificationEvent(title, content, deviceTokens);
    }
}
