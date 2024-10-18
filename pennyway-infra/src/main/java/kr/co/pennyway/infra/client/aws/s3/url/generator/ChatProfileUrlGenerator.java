package kr.co.pennyway.infra.client.aws.s3.url.generator;

import kr.co.pennyway.common.util.UUIDUtil;

import java.util.HashMap;
import java.util.Map;

public class ChatProfileUrlGenerator implements UrlGenerator {
    @Override
    public Map<String, String> generate(String ext, String userId, String chatId, String chatroomId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (chatroomId == null) {
            chatroomId = UUIDUtil.generateUUID();
        }
        Map<String, String> variablesMap = new HashMap<>();
        variablesMap.put("uuid", UUIDUtil.generateUUID());
        variablesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        variablesMap.put("ext", ext);
        variablesMap.put("chatroom_id", chatroomId);
        variablesMap.put("user_id", userId);
        return variablesMap;
    }
}

