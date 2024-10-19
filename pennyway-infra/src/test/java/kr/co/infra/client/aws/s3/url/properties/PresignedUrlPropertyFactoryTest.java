package kr.co.infra.client.aws.s3.url.properties;

import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.client.aws.s3.url.properties.ChatUrlProperty;
import kr.co.pennyway.infra.client.aws.s3.url.properties.FeedUrlProperty;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlProperty;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlPropertyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

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
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.createInstance("jpg", ObjectKeyType.FEED, 1L, null);

        PresignedUrlProperty property = factory.getProperty();
        assertTrue(property instanceof FeedUrlProperty);
        assertNotNull(ReflectionTestUtils.getField(property, "feedId"));
    }

    @Test
    @DisplayName("채팅 타입에 대해 PresignedUrlProperty를 생성한다.")
    void createPropertyChat() {
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.createInstance("png", ObjectKeyType.CHAT, 1L, 100L);

        PresignedUrlProperty property = factory.getProperty();
        assertTrue(property instanceof ChatUrlProperty);
        assertEquals(100L, ReflectionTestUtils.getField(property, "chatroomId"));
        assertNotNull(ReflectionTestUtils.getField(property, "chatId"));
    }

    @Test
    @DisplayName("잘못된 확장자로 생성 시 예외를 던진다.")
    void createPropertyInvalidExtension() {
        assertThrows(IllegalArgumentException.class, () ->
                PresignedUrlPropertyFactory.createInstance("gif", ObjectKeyType.PROFILE, 1L, null));
    }

    @Test
    @DisplayName("필수 파라미터가 누락된 경우 예외를 던진다.")
    void createPropertyMissingRequiredParameter() {
        assertThrows(NullPointerException.class, () ->
                PresignedUrlPropertyFactory.createInstance("jpg", ObjectKeyType.PROFILE, null, null));
    }

    @Test
    @DisplayName("PresignedUrlProperty는 자신의 변수들을 반환할 수 있다.")
    void createPropertyVariables() {
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.createInstance("jpg", ObjectKeyType.PROFILE, 1L, null);

        PresignedUrlProperty property = factory.getProperty();
        Map<String, String> variables = property.variables();

        assertNotNull(variables);
        assertEquals("1", variables.get("user_id"));
        assertNotNull(variables.get("uuid"));
        assertNotNull(variables.get("timestamp"));
        assertEquals("jpg", variables.get("ext"));
    }

    private PresignedUrlPropertyFactory createValidFactory(ObjectKeyType type) {
        return switch (type) {
            case PROFILE -> PresignedUrlPropertyFactory.createInstance("jpg", type, 1L, null);
            case FEED -> PresignedUrlPropertyFactory.createInstance("jpg", type, null, null);
            case CHATROOM_PROFILE -> PresignedUrlPropertyFactory.createInstance("jpg", type, null, null);
            case CHAT -> PresignedUrlPropertyFactory.createInstance("jpg", type, null, 100L);
            case CHAT_PROFILE -> PresignedUrlPropertyFactory.createInstance("jpg", type, 1L, 100L);
        };
    }
}
