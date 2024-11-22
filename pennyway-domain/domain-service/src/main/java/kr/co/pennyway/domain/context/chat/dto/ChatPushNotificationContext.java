package kr.co.pennyway.domain.context.chat.dto;

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
