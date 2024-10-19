package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.common.util.UUIDUtil;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Objects;
import java.util.Set;

public class PresignedUrlPropertyFactory {
    private static final Set<String> extensionSet = Set.of("jpg", "png", "jpeg");
    private final PresignedUrlProperty property;

    private PresignedUrlPropertyFactory(Builder builder) {
        this.property = switch (builder.type) {
            case PROFILE ->
                    new ProfileUrlProperty(builder.imageId, builder.timestamp, builder.ext, builder.type, builder.userId);
            case CHATROOM_PROFILE ->
                    new ChatRoomProfileUrlProperty(builder.imageId, builder.timestamp, builder.ext, builder.type, builder.chatroomId);
            case CHAT_PROFILE ->
                    new ChatProfileUrlProperty(builder.imageId, builder.timestamp, builder.ext, builder.type, builder.userId, builder.chatroomId);
            case CHAT ->
                    new ChatUrlProperty(builder.imageId, builder.timestamp, builder.ext, builder.type, builder.chatroomId, builder.chatId);
            case FEED ->
                    new FeedUrlProperty(builder.imageId, builder.timestamp, builder.ext, builder.type, builder.feedId);
        };
    }

    public static Builder create(String ext, ObjectKeyType type) {
        return new Builder(ext, type);
    }

    public PresignedUrlProperty getProperty() {
        return property;
    }

    public static class Builder {
        private final String imageId;
        private final String timestamp;
        private final String ext;
        private final ObjectKeyType type;
        private Long userId;
        private Long chatroomId;
        private Long chatId;
        private Long feedId;

        private Builder(String ext, ObjectKeyType type) {
            this.imageId = UUIDUtil.generateUUID();
            this.timestamp = String.valueOf(System.currentTimeMillis());
            this.ext = ext;
            this.type = type;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder chatroomId(Long chatroomId) {
            this.chatroomId = chatroomId;
            return this;
        }

        public Builder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public Builder feedId(Long feedId) {
            this.feedId = feedId;
            return this;
        }

        public PresignedUrlPropertyFactory build() {
            validate();

            return new PresignedUrlPropertyFactory(this);
        }

        private void validate() {
            Objects.requireNonNull(type, "타입은 필수입니다.");

            if (!extensionSet.contains(ext)) {
                throw new IllegalArgumentException("지원하지 않는 확장자입니다.");
            }

            if (ObjectKeyType.PROFILE.equals(type) && Objects.isNull(userId)) {
                throw new IllegalArgumentException("프로필 이미지를 위해 유저 ID는 필수입니다.");
            }
            if (ObjectKeyType.CHATROOM_PROFILE.equals(type) && Objects.isNull(chatroomId)) {
                throw new IllegalArgumentException("채팅방 이미지를 위해 채팅방 ID는 필수입니다.");
            }
            if (ObjectKeyType.CHAT_PROFILE.equals(type) && Objects.isNull(chatroomId)) {
                throw new IllegalArgumentException("채팅 프로필 이미지를 위해 채팅방 ID는 필수입니다.");
            }
            if (ObjectKeyType.CHAT.equals(type) && (Objects.isNull(chatroomId) || Objects.isNull(chatId))) {
                throw new IllegalArgumentException("채팅 이미지를 위해 채팅방 ID와 채팅 ID는 필수입니다.");
            }
            if (ObjectKeyType.FEED.equals(type) && Objects.isNull(feedId)) {
                throw new IllegalArgumentException("피드 이미지를 위해 피드 ID는 필수입니다.");
            }
        }
    }
}

