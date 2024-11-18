package kr.co.pennyway.domain.services.chat.context;

import java.util.List;

public record ChatPushNotificationContext(
        String senderName,
        String senderImageUrl,
        List<String> deviceTokens
) {
    public static ChatPushNotificationContext of(String senderName, String senderImageUrl, List<String> deviceTokens) {
        return new ChatPushNotificationContext(senderName, senderImageUrl, deviceTokens);
    }
}
