package kr.co.infra.client.aws.s3.url.generator;

import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.client.aws.s3.url.generator.UrlGenerator;
import kr.co.pennyway.infra.client.aws.s3.url.properties.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class UrlGeneratorTest {
    private final Logger log = Logger.getLogger(UrlGeneratorTest.class.getName());

    @Test
    @DisplayName("프로필 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlProfile() {
        PresignedUrlProperty property = new ProfileUrlProperty(1L, "jpg");

        String result = UrlGenerator.createDeleteUrl(property);

        log.info("deleteUrl: " + result);
        assertTrue(result.matches("delete/profile/1/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("채팅 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlChat() {
        PresignedUrlProperty property = new ProfileUrlProperty(1L, "jpg");

        String result = UrlGenerator.createDeleteUrl(property);

        log.info("deleteUrl: " + result);
        assertTrue(result.matches("delete/profile/1/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("채팅 프로필 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlChatProfile() {
        PresignedUrlProperty property = new ChatProfileUrlProperty(500L, 600L, "jpg");

        String result = UrlGenerator.createDeleteUrl(property);

        log.info("deleteUrl: " + result);
        assertTrue(result.matches("delete/chatroom/600/chat_profile/500/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("피드 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlFeed() {
        PresignedUrlProperty property = new FeedUrlProperty("jpg");

        String result = UrlGenerator.createDeleteUrl(property);

        log.info("deleteUrl: " + result);
        assertTrue(result.matches("delete/feed/[a-f0-9-]+/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("채팅방 프로필 타입에 대한 임시 저장 URL을 생성한다.")
    void createDeleteUrlChatRoomProfile() {
        PresignedUrlProperty property = new ChatRoomProfileUrlProperty("png");

        String result = UrlGenerator.createDeleteUrl(property);

        log.info("deleteUrl: " + result);
        assertTrue(result.matches("delete/chatroom/[a-f0-9-]+/[a-f0-9-]+_\\d+\\.png"));
    }

    @Test
    @DisplayName("프로필 타입에 대한 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlProfile() {
        PresignedUrlProperty property = new ProfileUrlProperty(1L, "jpg");
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        ActualIdProvider idProvider = ActualIdProvider.createInstanceOfProfile();
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(idProvider, deleteUrl);

        log.info("deleteUrl: " + deleteUrl + " -> originUrl: " + originUrl);
        assertTrue(originUrl.matches("profile/1/origin/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("채팅 타입에 대한 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlChat() {
        PresignedUrlProperty property = new ChatUrlProperty(300L, "jpg");
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        ActualIdProvider idProvider = ActualIdProvider.createInstanceOfChat(1000L);
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(idProvider, deleteUrl);

        log.info("deleteUrl: " + deleteUrl + " -> originUrl: " + originUrl);
        assertTrue(originUrl.matches("chatroom/300/chat/1000/origin/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("채팅 프로필 타입에 대한 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlChatProfile() {
        PresignedUrlProperty property = new ChatProfileUrlProperty(500L, 600L, "jpg");
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        ActualIdProvider idProvider = ActualIdProvider.createInstanceOfChatProfile();
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(idProvider, deleteUrl);

        log.info("deleteUrl: " + deleteUrl + " -> originUrl: " + originUrl);
        assertTrue(originUrl.matches("chatroom/600/chat_profile/500/origin/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("피드 타입에 대한 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlFeed() {
        PresignedUrlProperty property = new FeedUrlProperty("jpg");
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        ActualIdProvider idProvider = ActualIdProvider.createInstanceOfFeed(1000L);
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(idProvider, deleteUrl);

        log.info("deleteUrl: " + deleteUrl + " -> originUrl: " + originUrl);
        assertTrue(originUrl.matches("feed/1000/origin/[a-f0-9-]+_\\d+\\.jpg"));
    }

    @Test
    @DisplayName("채팅방 프로필 타입에 대한 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlChatRoomProfile() {
        PresignedUrlProperty property = new ChatRoomProfileUrlProperty("png");
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        ActualIdProvider idProvider = ActualIdProvider.createInstanceOfChatroomProfile(2000L);
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(idProvider, deleteUrl);

        log.info("deleteUrl: " + deleteUrl + " -> originUrl: " + originUrl);
        assertTrue(originUrl.matches("chatroom/2000/origin/[a-f0-9-]+_\\d+\\.png"));
    }

    @Test
    @DisplayName("잘못된 URL 형식의 임시 저장 URL을 원본 URL로 변환하려고 시도하면 예외를 던진다.")
    void convertDeleteToOriginUrlInvalidUrl() {
        String invalidUrl = "invalid/url/format";
        ActualIdProvider idProvider = ActualIdProvider.createInstanceOfProfile();
        assertThrows(IllegalArgumentException.class, () ->
                UrlGenerator.convertDeleteToOriginUrl(idProvider, invalidUrl));
    }

    @ParameterizedTest
    @EnumSource(ObjectKeyType.class)
    @DisplayName("모든 타입에 대해 임시 저장 URL을 원본 URL로 변환한다.")
    void convertDeleteToOriginUrlAllTypes(ObjectKeyType type) {
        PresignedUrlProperty property = createDummyProperty(type);
        String deleteUrl = UrlGenerator.createDeleteUrl(property);
        ActualIdProvider idProvider = createDummyActualIdProvider(type);
        String originUrl = UrlGenerator.convertDeleteToOriginUrl(idProvider, deleteUrl);

        assertTrue(originUrl.contains("origin"));
        assertFalse(originUrl.contains("delete"));

        switch (type) {
            case PROFILE -> assertTrue(originUrl.matches("profile/\\d+/origin/[a-f0-9-]+_\\d+\\.(jpg|png|jpeg)"));
            case FEED -> assertTrue(originUrl.matches("feed/\\d+/origin/[a-f0-9-]+_\\d+\\.(jpg|png|jpeg)"));
            case CHATROOM_PROFILE ->
                    assertTrue(originUrl.matches("chatroom/\\d+/origin/[a-f0-9-]+_\\d+\\.(jpg|png|jpeg)"));
            case CHAT ->
                    assertTrue(originUrl.matches("chatroom/\\d+/chat/\\d+/origin/[a-f0-9-]+_\\d+\\.(jpg|png|jpeg)"));
            case CHAT_PROFILE ->
                    assertTrue(originUrl.matches("chatroom/\\d+/chat_profile/\\d+/origin/[a-f0-9-]+_\\d+\\.(jpg|png|jpeg)"));
        }
    }

    private PresignedUrlProperty createDummyProperty(ObjectKeyType type) {
        return switch (type) {
            case PROFILE -> new ProfileUrlProperty(1L, "jpg");
            case FEED -> new FeedUrlProperty("png");
            case CHATROOM_PROFILE -> new ChatRoomProfileUrlProperty("jpeg");
            case CHAT -> new ChatUrlProperty(300L, "jpg");
            case CHAT_PROFILE -> new ChatProfileUrlProperty(500L, 600L, "png");
        };
    }

    private ActualIdProvider createDummyActualIdProvider(ObjectKeyType type) {
        return switch (type) {
            case PROFILE -> ActualIdProvider.createInstanceOfProfile();
            case FEED -> ActualIdProvider.createInstanceOfFeed(100L);
            case CHATROOM_PROFILE -> ActualIdProvider.createInstanceOfChatroomProfile(200L);
            case CHAT -> ActualIdProvider.createInstanceOfChat(400L);
            case CHAT_PROFILE -> ActualIdProvider.createInstanceOfChatProfile();
        };
    }
}
