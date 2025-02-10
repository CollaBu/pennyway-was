package kr.co.pennyway.infra.client.aws.s3;

import java.util.regex.Pattern;

public class ObjectKeyPattern {
    public static final String USER_ID_PATTERN = "([^/]+)";
    public static final String UUID_PATTERN = "([^_]+)";
    public static final String TIMESTAMP_PATTERN = "([^\\.]+)";
    public static final String EXT_PATTERN = "([^/]+)";
    public static final String FEED_ID_PATTERN = "([^/]+)";
    public static final String CHATROOM_ID_PATTERN = "([^/]+)";
    public static final String CHAT_ID_PATTERN = "([^/]+)";

    public static final Pattern PROFILE_PATTERN = Pattern.compile(
            createRegex("delete/profile/{userId}/{uuid}_{timestamp}.{ext}"));
    public static final Pattern FEED_PATTERN = Pattern.compile(
            createRegex("delete/feed/{feed_id}/{uuid}_{timestamp}.{ext}"));
    public static final Pattern CHATROOM_PROFILE_PATTERN = Pattern.compile(
            createRegex("delete/chatroom/{chatroom_id}/{uuid}_{timestamp}.{ext}"));
    public static final Pattern CHAT_PATTERN = Pattern.compile(
            createRegex("delete/chatroom/{chatroom_id}/chat/{chat_id}/{uuid}_{timestamp}.{ext}"));
    public static final Pattern CHAT_PROFILE_PATTERN = Pattern.compile(
            createRegex("delete/chatroom/{chatroom_id}/chat_profile/{userId}/{uuid}_{timestamp}.{ext}"));

    private static String createRegex(String template) {
        return template
                .replace("{userId}", USER_ID_PATTERN)
                .replace("{uuid}", UUID_PATTERN)
                .replace("{timestamp}", TIMESTAMP_PATTERN)
                .replace("{ext}", EXT_PATTERN)
                .replace("{feed_id}", FEED_ID_PATTERN)
                .replace("{chatroom_id}", CHATROOM_ID_PATTERN)
                .replace("{chat_id}", CHAT_ID_PATTERN);
    }
}
