package kr.co.pennyway.infra.common.event;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * FCM 푸시 알림에 필요한 정보를 담은 Event 클래스
 * <p>
 * 제목, 내용, 디바이스 토큰 리스트, 푸시 알림 이미지를 필드로 갖는다.
 */
public record NotificationEvent(
        String title,
        String content,
        List<String> deviceTokens,
        String imageUrl
) {
    public NotificationEvent {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("제목은 반드시 null 혹은 공백이 아니어야 합니다.");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("내용은 반드시 null 혹은 공백이 아니어야 합니다.");
        }
        if (deviceTokens == null) {
            throw new IllegalArgumentException("디바이스 토큰은 반드시 null이 아니어야 합니다.");
        }
    }

    public static NotificationEvent of(String title, String content, List<String> deviceTokens, String imageUrl) {
        return new NotificationEvent(title, content, deviceTokens, imageUrl);
    }

    public int deviceTokensSize() {
        return deviceTokens.size();
    }

    /**
     * 단일 메시지를 전송하기 위한 Message.Builder를 생성한다.
     */
    public Message.Builder buildSingleMessage() {
        return Message.builder().setNotification(toNotification()).setToken(deviceTokens.get(0));
    }

    /**
     * 다중 메시지를 전송하기 위한 MulticastMessage.Builder를 생성한다.
     */
    public MulticastMessage.Builder buildMulticastMessage() {
        return MulticastMessage.builder().setNotification(toNotification()).addAllTokens(deviceTokens);
    }

    private Notification toNotification() {
        return Notification.builder()
                .setTitle(title)
                .setBody(content)
                .setImage(imageUrl)
                .build();
    }
}
