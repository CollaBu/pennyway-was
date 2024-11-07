package kr.co.pennyway.batch.integration;

import kr.co.pennyway.batch.config.BatchDBTestConfig;
import kr.co.pennyway.batch.config.BatchIntegrationTest;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@BatchIntegrationTest
public class LastMessageIdIntegrationTest extends BatchDBTestConfig {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job lastMessageIdJob;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ChatMessageStatusRepository chatMessageStatusRepository;

    private JobParameters jobParameters;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    @Test
    @DisplayName("lastMessageId Job이 정상적으로 실행되어야 한다")
    void lastMessageIdJobTest() throws Exception {
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long roomId1 = 1L;
        Long roomId2 = 2L;

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        List<ChatMessageStatus> statuses = chatMessageStatusRepository.findAllByUserIdAndChatRoomIdIn(
                userId1,
                Arrays.asList(roomId1, roomId2)
        );

        assertEquals(2, statuses.size());
        log.debug("사용자 1번의 데이터: {}", statuses);

        // userId1, roomId1 검증
        Optional<ChatMessageStatus> status1 = statuses.stream()
                .filter(s -> s.getChatRoomId().equals(roomId1))
                .findFirst();
        assertTrue(status1.isPresent(), "userId1, roomId1 데이터가 존재해야 합니다.");
        assertEquals(100L, status1.get().getLastReadMessageId());

        // userId1, roomId2 검증
        Optional<ChatMessageStatus> status2 = statuses.stream()
                .filter(s -> s.getChatRoomId().equals(roomId2))
                .findFirst();
        assertTrue(status2.isPresent(), "userId1, roomId2 데이터가 존재해야 합니다.");
        assertEquals(200L, status2.get().getLastReadMessageId());

        // Redis 캐시가 남아있는지 확인
        assertEquals("100", redisTemplate.opsForValue().get(formatCacheKey(userId1, roomId1)));
        assertEquals("200", redisTemplate.opsForValue().get(formatCacheKey(userId1, roomId2)));
        assertEquals("300", redisTemplate.opsForValue().get(formatCacheKey(userId2, roomId1)));
    }

    @Test
    @DisplayName("Job 실행 시 일부 데이터가 누락되어도 나머지 데이터는 정상 처리되어야 한다")
    void jobWithPartialDataTest() throws Exception {
        // given
        Long userId = 1L;
        Long roomId = 1L;

        redisTemplate.opsForValue().set(formatCacheKey(userId, roomId), "invalid_value"); // Redis에는 있지만 value가 잘못된 데이터
        redisTemplate.opsForValue().set(formatCacheKey(2L, 2L), "300");

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus(), "Job이 정상 완료되어야 합니다.");

        Optional<ChatMessageStatus> validStatus = chatMessageStatusRepository.findByUserIdAndChatRoomId(2L, 2L);
        assertTrue(validStatus.isPresent(), "2번 사용자, 2번 채팅방의 lastMessageId 데이터가 존재해야 합니다.");
        assertEquals(300L, validStatus.get().getLastReadMessageId());

        Optional<ChatMessageStatus> invalidStatus = chatMessageStatusRepository.findByUserIdAndChatRoomId(userId, roomId);
        assertTrue(invalidStatus.isEmpty(), "1번 사용자, 1번 채팅방 (잘못된)lastMessageId 데이터가 존재하지 않아야 합니다.");
    }

    @Test
    @DisplayName("빈 데이터로 Job 실행 시 정상 완료되어야 한다")
    void emptyDataJobTest() throws Exception {
        // given
        cleanupTestData();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus(), "Job이 정상 완료되어야 합니다.");
        assertEquals(0, chatMessageStatusRepository.count());
    }

    private void setupTestData() {
        jobRepositoryTestUtils.removeJobExecutions();
        jobLauncherTestUtils.setJob(lastMessageIdJob);
        jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // Redis 테스트 데이터 설정
        redisTemplate.opsForValue().set(formatCacheKey(1L, 1L), "100");
        redisTemplate.opsForValue().set(formatCacheKey(1L, 2L), "200");
        redisTemplate.opsForValue().set(formatCacheKey(2L, 1L), "300");
    }

    private void cleanupTestData() {
        // Redis 데이터 정리
        Set<String> keys = redisTemplate.keys("chat:last_read:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // DB 데이터 정리
        chatMessageStatusRepository.deleteAll();
    }

    private String formatCacheKey(Long userId, Long chatRoomId) {
        return "chat:last_read:" + chatRoomId + ":" + userId;
    }
}
