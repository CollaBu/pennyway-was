package kr.co.pennyway.infra.client.aws.s3;

import java.util.HashMap;
import java.util.Map;

import kr.co.pennyway.common.util.UUIDUtil;

public class ChatUrlGenerator implements UrlGenerator {
	@Override
	public Map<String, String> generate(String ext, String userId, String chatId, String chatroomId) {
		Map<String, String> variablesMap = new HashMap<>();
		variablesMap.put("uuid", UUIDUtil.generateUUID());
		variablesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
		variablesMap.put("ext", ext);
		variablesMap.put("chatroom_id", chatroomId);
		variablesMap.put("chat_id", UUIDUtil.generateUUID());
		return variablesMap;
	}
}

