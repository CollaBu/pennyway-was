package kr.co.pennyway.domain.common.redis.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.pennyway.domain.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("사용자 세션 Redis 저장소 테스트")
@SpringBootTest(classes = {UserSessionRepositoryImpl.class, RedisConfig.class})
@ActiveProfiles("test")
public class UserSessionCustomRepositoryTest extends ContainerRedisTestConfig {
    @Autowired
    private UserSessionRepository userSessionRepository;

    private Long userId;
    private String deviceId;
    private String deviceName;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        userId = 1L;
        deviceId = "123456789";
        deviceName = "TestDevice";
        userSession = UserSession.of(deviceName);
    }

    @Test
    @DisplayName("사용자 세션 저장 및 조회 테스트")
    void saveAndFindUserSessionTest() throws JsonProcessingException {
        // given
        userSessionRepository.save(userId, deviceId, userSession);

        // when
        Optional<UserSession> foundSession = userSessionRepository.findUserSession(userId, deviceId);

        // then
        log.debug("foundSession: {}", foundSession);
        assertTrue(foundSession.isPresent());
        assertEquals(deviceName, foundSession.get().getDeviceName());
        assertEquals(UserStatus.ACTIVE_APP, foundSession.get().getStatus());
    }

    @Test
    @DisplayName("모든 사용자 세션 조회 테스트")
    void findAllUserSessionsTest() throws JsonProcessingException {
        // given
        String deviceId2 = "987654321";
        String deviceName2 = "TestDevice2";
        UserSession userSession2 = UserSession.of(deviceName2);
        userSessionRepository.save(userId, deviceId, userSession);
        userSessionRepository.save(userId, deviceId2, userSession2);

        // when
        Map<String, UserSession> allSessions = userSessionRepository.findAllUserSessions(userId);

        // then
        log.debug("allSessions: {}", allSessions);
        assertThat(allSessions).hasSize(2);
        assertTrue(allSessions.containsKey(deviceId));
        assertTrue(allSessions.containsKey(deviceId2));
    }

    @Test
    @DisplayName("세션 TTL 조회 및 업데이트 테스트")
    void sessionTtlTest() throws Exception {
        // given
        userSessionRepository.save(userId, deviceId, userSession);

        // when
        Thread.sleep(1000); // 1초 대기
        Long initialTtl = userSessionRepository.getSessionTtl(userId, deviceId);
        userSessionRepository.resetSessionTtl(userId, deviceId); // 세션 초기화
        Long updatedTtl = userSessionRepository.getSessionTtl(userId, deviceId);

        // then
        log.debug("initialTtl: {}, updatedTtl: {}", initialTtl, updatedTtl);
        assertNotNull(initialTtl);
        assertNotNull(updatedTtl);
        assertTrue(updatedTtl > initialTtl); // 약간의 오차 허용
    }

    @Test
    @DisplayName("사용자 세션 존재 여부 조회 테스트")
    void existsTest() {
        // Given
        userSessionRepository.save(userId, deviceId, userSession);

        // When
        boolean exists = userSessionRepository.exists(userId, deviceId);

        // Then
        assertTrue(exists);

        // When
        boolean notExists = userSessionRepository.exists(userId, "nonExistentDevice");

        // Then
        assertFalse(notExists);
    }

    @Test
    @DisplayName("사용자 세션 삭제 테스트")
    void deleteUserSessionTest() throws JsonProcessingException {
        // given
        userSessionRepository.save(userId, deviceId, userSession);

        // when
        userSessionRepository.delete(userId, deviceId);

        // then
        Optional<UserSession> deletedSession = userSessionRepository.findUserSession(userId, deviceId);
        assertFalse(deletedSession.isPresent());
    }

    @Test
    @DisplayName("사용자 세션 상태 업데이트 테스트 (채팅방으로 이동)")
    void updateUserSessionStatusTest() {
        // given
        userSessionRepository.save(userId, deviceId, userSession);

        // when
        UserSession updatedSession = userSessionRepository.findUserSession(userId, deviceId).get();
        updatedSession.updateStatus(UserStatus.ACTIVE_CHAT_ROOM, 123L);
        userSessionRepository.save(userId, deviceId, updatedSession);

        // then
        log.debug("updatedSession: {} to {}", userSession, updatedSession);
        UserSession foundSession = userSessionRepository.findUserSession(userId, deviceId).get();
        assertEquals(UserStatus.ACTIVE_CHAT_ROOM, foundSession.getStatus());
        assertEquals(123L, foundSession.getCurrentChatRoomId());
    }

    @Test
    @DisplayName("사용자 세션 마지막 활동 시간 업데이트 테스트")
    void updateLastActiveAtTest() throws Exception {
        // given
        userSessionRepository.save(userId, deviceId, userSession);
        LocalDateTime initialLastActiveAt = userSession.getLastActiveAt();

        // when
        Thread.sleep(1000); // 1초 대기
        UserSession updatedSession = userSessionRepository.findUserSession(userId, deviceId).get();
        updatedSession.updateLastActiveAt();
        userSessionRepository.save(userId, deviceId, updatedSession);

        // then
        UserSession foundSession = userSessionRepository.findUserSession(userId, deviceId).get();
        assertTrue(foundSession.getLastActiveAt().isAfter(initialLastActiveAt));
    }

    @AfterEach
    void tearDown() {
        userSessionRepository.delete(userId, deviceId);
    }
}
