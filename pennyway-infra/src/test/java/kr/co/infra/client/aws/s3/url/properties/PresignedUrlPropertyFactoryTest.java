package kr.co.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.client.aws.s3.url.properties.ChatUrlProperty;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlProperty;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlPropertyFactory;
import kr.co.pennyway.infra.client.aws.s3.url.properties.ProfileUrlProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PresignedUrlPropertyFactoryTest {
    @ParameterizedTest
    @EnumSource(ObjectKeyType.class)
    @DisplayName("모든 타입에 대해 PresignedUrlProperty를 생성한다.")
    void createPropertyAllTypes(ObjectKeyType type) {
        PresignedUrlPropertyFactory factory = createValidFactory(type);
        PresignedUrlProperty property = factory.getProperty();

        assertNotNull(property);
        assertEquals(type, property.type());
        assertNotNull(property.imageId());
        assertNotNull(property.timestamp());
        assertEquals("jpg", property.ext());
    }

    @Test
    @DisplayName("피드 타입에 대해 PresignedUrlProperty를 생성한다.")
    void createPropertyProfile() {
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.create("jpg", ObjectKeyType.PROFILE)
                .userId(1L)
                .build();

        PresignedUrlProperty property = factory.getProperty();
        assertTrue(property instanceof ProfileUrlProperty);
        assertEquals(1L, ((ProfileUrlProperty) property).userId());
    }

    @Test
    @DisplayName("피드 타입에 대해 PresignedUrlProperty를 생성한다.")
    void createPropertyChat() {
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.create("png", ObjectKeyType.CHAT)
                .chatroomId(100L)
                .chatId(200L)
                .build();

        PresignedUrlProperty property = factory.getProperty();
        assertTrue(property instanceof ChatUrlProperty);
        assertEquals(100L, ((ChatUrlProperty) property).chatroomId());
        assertEquals(200L, ((ChatUrlProperty) property).chatId());
    }

    @Test
    @DisplayName("피드 타입에 대해 PresignedUrlProperty를 생성한다.")
    void createPropertyInvalidExtension() {
        assertThrows(IllegalArgumentException.class, () ->
                PresignedUrlPropertyFactory.create("gif", ObjectKeyType.PROFILE)
                        .userId(1L)
                        .build());
    }

    @Test
    @DisplayName("필수 파라미터가 누락된 경우 예외를 던진다.")
    void createPropertyMissingRequiredParameter() {
        assertThrows(IllegalArgumentException.class, () ->
                PresignedUrlPropertyFactory.create("jpg", ObjectKeyType.PROFILE)
                        .build());
    }

    @Test
    @DisplayName("PresignedUrlProperty는 자신의 변수들을 반환할 수 있다.")
    void createPropertyVariables() {
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.create("jpg", ObjectKeyType.PROFILE)
                .userId(1L)
                .build();

        PresignedUrlProperty property = factory.getProperty();
        Map<String, String> variables = property.variables();

        assertNotNull(variables);
        assertEquals("1", variables.get("user_id"));
        assertNotNull(variables.get("uuid"));
        assertNotNull(variables.get("timestamp"));
        assertEquals("jpg", variables.get("ext"));
    }

    private PresignedUrlPropertyFactory createValidFactory(ObjectKeyType type) {
        PresignedUrlPropertyFactory.Builder builder = PresignedUrlPropertyFactory.create("jpg", type);
        switch (type) {
            case PROFILE -> builder.userId(1L);
            case FEED -> builder.feedId(100L);
            case CHATROOM_PROFILE -> builder.chatroomId(200L);
            case CHAT -> builder.chatroomId(300L).chatId(400L);
            case CHAT_PROFILE -> builder.userId(500L).chatroomId(600L);
        }
        return builder.build();
    }
}
