package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.common.util.UUIDUtil;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;
import java.util.Objects;

public class ChatUrlProperty extends BaseUrlProperty {
    private final Long chatroomId;
    private final String chatId;

    public ChatUrlProperty(Long chatroomId, String ext) {
        super(ext, ObjectKeyType.CHAT);
        this.chatroomId = Objects.requireNonNull(chatroomId, "채팅방 아이디는 필수입니다.");
        this.chatId = UUIDUtil.generateUUID();
    }

    @Override
    public Map<String, String> variables() {
        return Map.of(
                "chatroom_id", chatroomId.toString(),
                "chat_id", chatId,
                "uuid", imageId,
                "timestamp", timestamp,
                "ext", ext
        );
    }
}
