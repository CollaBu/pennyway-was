package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public enum ChatRoomFixture {
    PRIVATE_CHAT_ROOM("페니웨이", "페니웨이 채팅방입니다.", "delete/chatroom/1/fsdflasdfa_12121210.jpg", 123456),
    PUBLIC_CHAT_ROOM("페니웨이", "페니웨이 채팅방입니다.", "delete/chatroom/1/fsdflasdfa_12121210.jpg", null);

    private final String title;
    private final String description;
    private final String backgroundImageUrl;
    private final Integer password;

    ChatRoomFixture(String title, String description, String backgroundImageUrl, Integer password) {
        this.title = title;
        this.description = description;
        this.backgroundImageUrl = backgroundImageUrl;
        this.password = password;
    }

    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .id(1L)
                .title(title)
                .description(description)
                .backgroundImageUrl(backgroundImageUrl)
                .password(password)
                .build();
    }
}
