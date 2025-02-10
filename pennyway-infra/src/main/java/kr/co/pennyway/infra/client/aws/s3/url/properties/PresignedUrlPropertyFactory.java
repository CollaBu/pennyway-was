package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

public class PresignedUrlPropertyFactory {
    private final PresignedUrlProperty property;

    private PresignedUrlPropertyFactory(Long userId, String ext, ObjectKeyType type, Long chatRoomId) {
        this.property = switch (type) {
            case PROFILE -> new ProfileUrlProperty(userId, ext);
            case CHATROOM_PROFILE -> new ChatRoomProfileUrlProperty(ext);
            case CHAT_PROFILE -> new ChatProfileUrlProperty(userId, chatRoomId, ext);
            case CHAT -> new ChatUrlProperty(chatRoomId, ext);
            case FEED -> new FeedUrlProperty(ext);
        };
    }

    public static PresignedUrlPropertyFactory createInstance(String ext, ObjectKeyType type, Long userId, Long chatRoomId) {
        return new PresignedUrlPropertyFactory(userId, ext, type, chatRoomId);
    }

    public PresignedUrlProperty getProperty() {
        return property;
    }
}

