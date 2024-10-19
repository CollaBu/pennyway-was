package kr.co.pennyway.infra.client.aws.s3.url.properties;

import kr.co.pennyway.common.util.UUIDUtil;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.util.Map;

public class ChatRoomProfileUrlProperty extends BaseUrlProperty {
    private final String chatroomId;

    public ChatRoomProfileUrlProperty(String ext) {
        super(ext, ObjectKeyType.CHATROOM_PROFILE);
        this.chatroomId = UUIDUtil.generateUUID();
    }

    @Override
    public Map<String, String> variables() {
        return Map.of(
                "chatroom_id", chatroomId,
                "uuid", imageId,
                "timestamp", timestamp,
                "ext", ext
        );
    }
}