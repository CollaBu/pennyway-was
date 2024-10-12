package kr.co.pennyway.api.config.fixture;

import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;

public enum ChatRoomFixture {
    DEFAULT_CHAT_ROOM("페니웨이", "페니웨이 채팅방입니다.", "https://pennyway.co.kr/background.jpg", 123456);;

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
                .title(title)
                .description(description)
                .backgroundImageUrl(backgroundImageUrl)
                .password(password)
                .build();
    }
}
