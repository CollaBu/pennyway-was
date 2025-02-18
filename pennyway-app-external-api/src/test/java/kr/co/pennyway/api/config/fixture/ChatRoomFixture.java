package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomReq;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public enum ChatRoomFixture {
    PRIVATE_CHAT_ROOM("페니웨이", "페니웨이 채팅방입니다.", "delete/chatroom/1/test-uuid_123.jpg", "123456"),
    PUBLIC_CHAT_ROOM("페니웨이", "페니웨이 채팅방입니다.", "delete/chatroom/1/test-uuid_123.jpg", null);

    private static final String originImageUrl = "chatroom/1/test-uuid_123.jpg";
    private final String title;
    private final String description;
    private final String backgroundImageUrl;
    private final String password;

    ChatRoomFixture(String title, String description, String backgroundImageUrl, String password) {
        this.title = title;
        this.description = description;
        this.backgroundImageUrl = backgroundImageUrl;
        this.password = password;
    }

    public static String getOriginImageUrl() {
        return originImageUrl;
    }

    public ChatRoom toEntity(Long id) {
        return ChatRoom.builder()
                .id(id)
                .title(title)
                .description(description)
                .backgroundImageUrl(backgroundImageUrl)
                .password(password != null ? Integer.valueOf(password) : null)
                .build();
    }

    public ChatRoomReq.Create toCreateRequest() {
        return new ChatRoomReq.Create(title, description, password, backgroundImageUrl);
    }
}
