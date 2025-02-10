package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;
import java.util.Objects;

public class ChatProfileUrlProperty extends BaseUrlProperty {
    private final Long userId;
    private final Long chatroomId;

    public ChatProfileUrlProperty(Long userId, Long chatroomId, String ext) {
        super(ext, ObjectKeyType.CHAT_PROFILE);
        this.userId = Objects.requireNonNull(userId, "유저 아이디는 필수입니다.");
        this.chatroomId = chatroomId;
    }

    @Override
    public Map<String, String> variables() {
        return Map.of(
                "user_id", userId.toString(),
                "chatroom_id", chatroomId.toString(),
                "uuid", imageId,
                "timestamp", timestamp,
                "ext", ext
        );
    }
}