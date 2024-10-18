package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;
import java.util.Objects;

public record ChatRoomProfileUrlProperty(
        String imageId,
        String timestamp,
        String ext,
        ObjectKeyType type,
        Long chatroomId
) implements PresignedUrlProperty {
    public ChatRoomProfileUrlProperty {
        Objects.requireNonNull(imageId, "이미지 아이디는 필수입니다.");
        Objects.requireNonNull(timestamp, "타임스탬프는 필수입니다.");
        Objects.requireNonNull(ext, "확장자는 필수입니다.");
        assert type == ObjectKeyType.CHATROOM_PROFILE : "타입은 채팅방 프로필이어야 합니다.";
        Objects.requireNonNull(chatroomId, "채팅방 아이디는 필수입니다.");
    }

    @Override
    public Map<String, String> variables() {
        return Map.of(
                "chatroom_id", chatroomId.toString(),
                "uuid", imageId,
                "timestamp", timestamp,
                "ext", ext
        );
    }
}
