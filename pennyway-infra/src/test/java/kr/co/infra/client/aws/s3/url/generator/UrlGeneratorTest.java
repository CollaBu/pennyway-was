package kr.co.infra.client.aws.s3.url.generator;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.client.aws.s3.url.generator.UrlGenerator;
import kr.co.pennyway.infra.client.aws.s3.url.properties.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlGeneratorTest {
    @Test
    @DisplayName("프로필 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlProfile() {
        PresignedUrlProperty property = new ProfileUrlProperty(
                "123e4567-e89b-12d3-a456-426614174000",
                "1634567890",
                "jpg",
                ObjectKeyType.PROFILE,
                1L
        );

        String result = UrlGenerator.createDeleteUrl(property);
        assertEquals("delete/profile/1/123e4567-e89b-12d3-a456-426614174000_1634567890.jpg", result);
    }

    @Test
    @DisplayName("채팅 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlChat() {
        PresignedUrlProperty property = new ChatUrlProperty(
                "123e4567-e89b-12d3-a456-426614174000",
                "1634567890",
                "png",
                ObjectKeyType.CHAT,
                100L,
                200L
        );

        String result = UrlGenerator.createDeleteUrl(property);
        assertEquals("delete/chatroom/100/chat/200/123e4567-e89b-12d3-a456-426614174000_1634567890.png", result);
    }

    @Test
    @DisplayName("채팅 프로필 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlChatProfile() {
        PresignedUrlProperty property = new ChatProfileUrlProperty(
                "123e4567-e89b-12d3-a456-426614174000",
                "1634567890",
                "jpg",
                ObjectKeyType.CHAT_PROFILE,
                300L,
                400L
        );

        String result = UrlGenerator.createDeleteUrl(property);
        assertEquals("delete/chatroom/400/chat_profile/300/123e4567-e89b-12d3-a456-426614174000_1634567890.jpg", result);
    }

    @Test
    @DisplayName("피드 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlFeed() {
        PresignedUrlProperty property = new FeedUrlProperty(
                "123e4567-e89b-12d3-a456-426614174000",
                "1634567890",
                "jpeg",
                ObjectKeyType.FEED,
                500L
        );

        String result = UrlGenerator.createDeleteUrl(property);
        assertEquals("delete/feed/500/123e4567-e89b-12d3-a456-426614174000_1634567890.jpeg", result);
    }

    @Test
    @DisplayName("채팅방 프로필 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlChatRoomProfile() {
        PresignedUrlProperty property = new ChatRoomProfileUrlProperty(
                "123e4567-e89b-12d3-a456-426614174000",
                "1634567890",
                "png",
                ObjectKeyType.CHATROOM_PROFILE,
                600L
        );

        String result = UrlGenerator.createDeleteUrl(property);
        assertEquals("delete/chatroom/600/123e4567-e89b-12d3-a456-426614174000_1634567890.png", result);
    }

    @Test
    @DisplayName("프로필 타입에 대한 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlProfile() {
        String deleteUrl = "delete/profile/1/123e4567-e89b-12d3-a456-426614174000_1634567890.jpg";
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(ObjectKeyType.PROFILE, deleteUrl);

        assertEquals("profile/1/origin/123e4567-e89b-12d3-a456-426614174000_1634567890.jpg", originUrl);
    }

    @Test
    @DisplayName("잘못된 URL 형식의 임시 저장 URL을 원본 URL로 변환하려고 시도하면 예외를 던진다.")
    void convertDeleteToOriginUrlInvalidUrl() {
        String invalidUrl = "invalid/url/format";
        assertThrows(IllegalArgumentException.class, () ->
                UrlGenerator.convertDeleteToOriginUrl(ObjectKeyType.PROFILE, invalidUrl));
    }

    @ParameterizedTest
    @EnumSource(ObjectKeyType.class)
    @DisplayName("모든 타입에 대해 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlAllTypes(ObjectKeyType type) {
        PresignedUrlProperty property = createDummyProperty(type);
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(type, deleteUrl);

        String expectedOriginUrl = createExpectedOriginUrl(type, property);
        assertEquals(expectedOriginUrl, originUrl, "For type: " + type);
    }

    private PresignedUrlProperty createDummyProperty(ObjectKeyType type) {
        return switch (type) {
            case PROFILE -> new ProfileUrlProperty("dummy-uuid", "1234567890", "jpg", type, 1L);
            case FEED -> new FeedUrlProperty("dummy-uuid", "1234567890", "png", type, 100L);
            case CHATROOM_PROFILE -> new ChatRoomProfileUrlProperty("dummy-uuid", "1234567890", "jpeg", type, 200L);
            case CHAT -> new ChatUrlProperty("dummy-uuid", "1234567890", "jpg", type, 300L, 400L);
            case CHAT_PROFILE -> new ChatProfileUrlProperty("dummy-uuid", "1234567890", "png", type, 500L, 600L);
        };
    }

    private String createExpectedOriginUrl(ObjectKeyType type, PresignedUrlProperty property) {
        return switch (type) {
            case PROFILE -> String.format("profile/%d/origin/%s_%s.%s",
                    ((ProfileUrlProperty) property).userId(), property.imageId(), property.timestamp(), property.ext());
            case FEED -> String.format("feed/%d/origin/%s_%s.%s",
                    ((FeedUrlProperty) property).feedId(), property.imageId(), property.timestamp(), property.ext());
            case CHATROOM_PROFILE -> String.format("chatroom/%d/origin/%s_%s.%s",
                    ((ChatRoomProfileUrlProperty) property).chatroomId(), property.imageId(), property.timestamp(), property.ext());
            case CHAT -> String.format("chatroom/%d/chat/%d/origin/%s_%s.%s",
                    ((ChatUrlProperty) property).chatroomId(), ((ChatUrlProperty) property).chatId(),
                    property.imageId(), property.timestamp(), property.ext());
            case CHAT_PROFILE -> String.format("chatroom/%d/chat_profile/%d/origin/%s_%s.%s",
                    ((ChatProfileUrlProperty) property).chatroomId(), ((ChatProfileUrlProperty) property).userId(),
                    property.imageId(), property.timestamp(), property.ext());
        };
    }
}
