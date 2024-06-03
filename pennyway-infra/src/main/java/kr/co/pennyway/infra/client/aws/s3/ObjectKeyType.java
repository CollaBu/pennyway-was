package kr.co.pennyway.infra.client.aws.s3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ObjectKeyType {
	PROFILE("1", "PROFILE", "delete/profile/{userId}/{uuid}_{timestamp}.{ext}", "profile/{userId}/origin/{uuid}_{timestamp}.{ext}"),
	FEED("2", "FEED", "delete/feed/{feed_id}/{uuid}_{timestamp}.{ext}", "feed/{feed_id}/origin/{uuid}_{timestamp}.{ext}"),
	CHATROOM_PROFILE("3", "CHATROOM_PROFILE", "delete/chatroom/{chatroom_id}/{uuid}_{timestamp}.{ext}",
			"chatroom/{chatroom_id}/origin/{uuid}_{timestamp}.{ext}"),
	CHAT("4", "CHAT", "delete/chatroom/{chatroom_id}/chat/{chat_id}/{uuid}_{timestamp}.{ext}",
			"chatroom/{chatroom_id}/chat/{chat_id}/origin/{uuid}_{timestamp}.{ext}"),
	CHAT_PROFILE("5", "CHAT_PROFILE", "delete/chatroom/{chatroom_id}/chat_profile/{userId}/{uuid}_{timestamp}.{ext}",
			"chatroom/{chatroom_id}/chat_profile/{userId}/origin/{uuid}_{timestamp}.{ext}");

	private final String code;
	private final String type;
	private final String deleteTemplate;
	private final String originTemplate;

	public String getCode() {
		return code;
	}

	public String getType() {
		return type;
	}

	public String getDeleteTemplate() {
		return deleteTemplate;
	}

	public String convertDeleteKeyToOriginKey(String deleteKey) {
		String regex = deleteTemplate
				.replace("{userId}", "([^/]+)")
				.replace("{uuid}", "([^_]+)")
				.replace("{timestamp}", "([^\\.]+)")
				.replace("{ext}", "([^/]+)")
				.replace("{feed_id}", "([^/]+)")
				.replace("{chatroom_id}", "([^/]+)")
				.replace("{chat_id}", "([^/]+)");

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(deleteKey);

		if (matcher.matches()) {
			String originKey = originTemplate;
			for (int i = 1; i <= matcher.groupCount(); i++) {
				originKey = originKey.replaceFirst("\\{[^}]+\\}", matcher.group(i));
			}
			return originKey;
		}

		throw new IllegalArgumentException("No matching ObjectKeyType for deleteKey: " + deleteKey);
	}
}
