package kr.co.pennyway.infra.client.aws.s3;

import java.util.Arrays;
import java.util.List;

public enum ObjectKeyType {
    PROFILE("1", "PROFILE", "delete/profile/{user_id}/{uuid}_{timestamp}.{ext}", "profile/{user_id}/origin/{uuid}_{timestamp}.{ext}"),
    FEED("2", "FEED", "delete/feed/{feed_id}/{uuid}_{timestamp}.{ext}", "feed/{feed_id}/origin/{uuid}_{timestamp}.{ext}", "feed_id"),
    CHATROOM_PROFILE("3", "CHATROOM_PROFILE", "delete/chatroom/{chatroom_id}/{uuid}_{timestamp}.{ext}",
            "chatroom/{chatroom_id}/origin/{uuid}_{timestamp}.{ext}", "chatroom_id"),
    CHAT("4", "CHAT", "delete/chatroom/{chatroom_id}/chat/{chat_id}/{uuid}_{timestamp}.{ext}",
            "chatroom/{chatroom_id}/chat/{chat_id}/origin/{uuid}_{timestamp}.{ext}", "chat_id"),
    CHAT_PROFILE("5", "CHAT_PROFILE", "delete/chatroom/{chatroom_id}/chat_profile/{user_id}/{uuid}_{timestamp}.{ext}",
            "chatroom/{chatroom_id}/chat_profile/{user_id}/origin/{uuid}_{timestamp}.{ext}");

    private final String code;
    private final String type;
    private final String deleteTemplate;
    private final String originTemplate;
    private final String[] requiredExchangeParams;

    ObjectKeyType(String code, String type, String deleteTemplate, String originTemplate, String... requiredExchangeParams) {
        this.code = code;
        this.type = type;
        this.deleteTemplate = deleteTemplate;
        this.originTemplate = originTemplate;
        this.requiredExchangeParams = requiredExchangeParams;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public List<String> getRequiredParams() {
        return Arrays.asList(requiredExchangeParams);
    }

    public String getDeleteTemplate() {
        return deleteTemplate;
    }

    public String getOriginTemplate() {
        return originTemplate;
    }
}
