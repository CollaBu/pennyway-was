package kr.co.pennyway.infra.client.aws.s3;

import java.util.HashMap;
import java.util.Map;

import kr.co.pennyway.common.util.UUIDUtil;

public class ProfileUrlGenerator implements UrlGenerator {
	@Override
	public Map<String, String> generate(String type, String ext, String userId, String chatroomId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 필수입니다.");
		}
		Map<String, String> variablesMap = new HashMap<>();
		variablesMap.put("type", type);
		variablesMap.put("ext", ext);
		variablesMap.put("userId", userId);
		variablesMap.put("uuid", UUIDUtil.generateUUID());
		variablesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
		return variablesMap;
	}
}
