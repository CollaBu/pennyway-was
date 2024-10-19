package kr.co.pennyway.infra.client.aws.s3;

import java.util.HashMap;
import java.util.Map;

/**
 * 임시 저장 URL에서 임의로 설정된 ID를 실제 ID로 변경하기 위한 정보를 제공하는 클래스
 */
public final class ActualIdProvider {
    private final ObjectKeyType type;
    private final Map<String, String> actualIds;

    private ActualIdProvider(ObjectKeyType type, Map<String, String> actualIds) {
        this.type = type;
        this.actualIds = actualIds;
    }

    /**
     * 프로필 이미지 URL을 생성하기 위한 ActualIdProvider 인스턴스를 생성합니다.
     */
    public static ActualIdProvider createInstanceOfProfile() {
        return createEmptyInstance(ObjectKeyType.PROFILE);
    }

    /**
     * 피드 이미지 URL을 생성하기 위한 ActualIdProvider 인스턴스를 생성합니다.
     *
     * @param feedId 실제 피드 ID
     */
    public static ActualIdProvider createInstanceOfFeed(Long feedId) {
        Map<String, String> ids = new HashMap<>();
        ids.put("feed_id", feedId.toString());
        return new ActualIdProvider(ObjectKeyType.FEED, ids);
    }

    /**
     * 채팅방 프로필 이미지 URL을 생성하기 위한 ActualIdProvider 인스턴스를 생성합니다.
     *
     * @param chatroomId 실제 채팅방 ID
     */
    public static ActualIdProvider createInstanceOfChatroomProfile(Long chatroomId) {
        Map<String, String> ids = new HashMap<>();
        ids.put("chatroom_id", chatroomId.toString());
        return new ActualIdProvider(ObjectKeyType.CHATROOM_PROFILE, ids);
    }

    /**
     * 채팅방 이미지 URL을 생성하기 위한 ActualIdProvider 인스턴스를 생성합니다.
     *
     * @param chatId 실제 채팅 ID
     */
    public static ActualIdProvider createInstanceOfChat(Long chatId) {
        Map<String, String> ids = new HashMap<>();
        ids.put("chat_id", chatId.toString());
        return new ActualIdProvider(ObjectKeyType.CHAT, ids);
    }

    /**
     * 채팅 프로필 이미지 URL을 생성하기 위한 ActualIdProvider 인스턴스를 생성합니다.
     */
    public static ActualIdProvider createInstanceOfChatProfile() {
        return createEmptyInstance(ObjectKeyType.CHAT_PROFILE);
    }

    private static ActualIdProvider createEmptyInstance(ObjectKeyType type) {
        return new ActualIdProvider(type, new HashMap<>());
    }

    public Map<String, String> getActualIds() {
        return actualIds;
    }

    public ObjectKeyType getType() {
        return type;
    }
}
