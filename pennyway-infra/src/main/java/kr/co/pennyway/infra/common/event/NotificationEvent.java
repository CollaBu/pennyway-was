package kr.co.pennyway.infra.common.event;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * FCM 푸시 알림에 필요한 정보를 담은 Event 클래스
 * <p>
 * 제목, 내용, 디바이스 토큰 리스트, 푸시 알림 이미지를 필드로 갖는다.
 */
public record NotificationEvent(
        String title,
        String content,
        List<String> deviceTokens,
        String imageUrl,
        Map<String, String> data
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
        return new NotificationEvent(title, content, deviceTokens, imageUrl, null);
    }

    /**
     * 추가 데이터를 포함하는 NotificationEvent를 생성한다.
     *
     * @param data : null을 허용하지 않으며, 반드시 null인 key, 혹은 value가 존재하지 않아야 한다.
     */
    public static NotificationEvent of(String title, String content, List<String> deviceTokens, String imageUrl, Map<String, String> data) {
        Assert.notNull(data, "추가 데이터는 null이 아니어야 합니다.");

        return new NotificationEvent(title, content, deviceTokens, imageUrl, data);
    }

    public int deviceTokensSize() {
        return deviceTokens.size();
    }

    /**
     * 단일 메시지를 전송하기 위한 Message.Builder를 생성한다.
     */
    public Message.Builder buildSingleMessage() {
        Message.Builder builder = Message.builder().setNotification(toNotification()).setToken(deviceTokens.get(0));

        if (data != null) {
            builder.putAllData(data);
        }

        return builder;
    }

    /**
     * 다중 메시지를 전송하기 위한 MulticastMessage.Builder를 생성한다.
     */
    public MulticastMessage.Builder buildMulticastMessage() {
        MulticastMessage.Builder builder = MulticastMessage.builder().setNotification(toNotification()).addAllTokens(deviceTokens);

        if (data != null) {
            builder.putAllData(data);
        }

        return builder;
    }

    private Notification toNotification() {
        return Notification.builder()
                .setTitle(title)
                .setBody(content)
                .setImage(imageUrl)
                .build();
    }
}
