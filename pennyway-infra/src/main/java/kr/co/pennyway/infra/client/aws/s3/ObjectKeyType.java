package kr.co.pennyway.infra.client.aws.s3;

import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public enum ObjectKeyType {
    PROFILE("1", "PROFILE", "delete/profile/{user_id}/{uuid}_{timestamp}.{ext}", "profile/{user_id}/origin/{uuid}_{timestamp}.{ext}"),
    FEED("2", "FEED", "delete/feed/{feed_id}/{uuid}_{timestamp}.{ext}", "feed/{feed_id}/origin/{uuid}_{timestamp}.{ext}"),
    CHATROOM_PROFILE("3", "CHATROOM_PROFILE", "delete/chatroom/{chatroom_id}/{uuid}_{timestamp}.{ext}",
            "chatroom/{chatroom_id}/origin/{uuid}_{timestamp}.{ext}"),
    CHAT("4", "CHAT", "delete/chatroom/{chatroom_id}/chat/{chat_id}/{uuid}_{timestamp}.{ext}",
            "chatroom/{chatroom_id}/chat/{chat_id}/origin/{uuid}_{timestamp}.{ext}"),
    CHAT_PROFILE("5", "CHAT_PROFILE", "delete/chatroom/{chatroom_id}/chat_profile/user_id}/{uuid}_{timestamp}.{ext}",
            "chatroom/{chatroom_id}/chat_profile/{user_id}/origin/{uuid}_{timestamp}.{ext}");

    private final String code;
    private final String type;
    private final String deleteTemplate;
    private final String originTemplate;

    public static String convertDeleteKeyToOriginKey(String deleteKey, Pattern pattern, String originTemplate) {
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

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getDeleteTemplate() {
        return deleteTemplate;
    }

    public String getOriginTemplate() {
        return originTemplate;
    }

    public String convertDeleteKeyToOriginKey(String deleteKey) {
        Pattern pattern = switch (this) {
            case PROFILE -> ObjectKeyPattern.PROFILE_PATTERN;
            case FEED -> ObjectKeyPattern.FEED_PATTERN;
            case CHATROOM_PROFILE -> ObjectKeyPattern.CHATROOM_PROFILE_PATTERN;
            case CHAT -> ObjectKeyPattern.CHAT_PATTERN;
            case CHAT_PROFILE -> ObjectKeyPattern.CHAT_PROFILE_PATTERN;
            default -> throw new IllegalArgumentException("Unknown ObjectKeyType: " + this);
        };
        return convertDeleteKeyToOriginKey(deleteKey, pattern, this.originTemplate);
    }
}
