package kr.co.pennyway.batch.job;

import kr.co.pennyway.batch.common.dto.KeyValue;
import kr.co.pennyway.batch.processor.LastMessageIdProcessor;
import kr.co.pennyway.batch.reader.LastMessageIdReader;
import kr.co.pennyway.batch.writer.LastMessageIdWriter;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBatchTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class LastMessageIdJobBatchTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private Cursor<String> cursor;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ChatMessageStatusRepository repository;

    private LastMessageIdReader reader;
    private LastMessageIdProcessor processor;
    private LastMessageIdWriter writer;

    @BeforeEach
    void setUp() {
        reader = new LastMessageIdReader(redisTemplate, cursor);
        processor = new LastMessageIdProcessor();
        writer = new LastMessageIdWriter(repository);
    }

    @Test
    @DisplayName("Reader - Redis에서 키/값을 정상적으로 읽어오는지 테스트")
    void readerTest() throws Exception {
        // given
        String key = "chat:last_read:1:2";
        String value = "100";

        given(cursor.hasNext()).willReturn(true, false);
        given(cursor.next()).willReturn(key);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(value);

        // when
        KeyValue result = reader.read();

        // then
        assertNotNull(result);
        assertEquals(key, result.key());
        assertEquals(value, result.value());
        assertNull(reader.read()); // 두 번째 읽기에서는 null 반환 확인
    }

    @Test
    @DisplayName("Reader - value가 null인 경우 null 반환 확인")
    void nullValueTest() throws Exception {
        // given
        String key = "chat:last_read:1:2";

        given(cursor.hasNext()).willReturn(true);
        given(cursor.next()).willReturn(key);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(null);

        // when
        KeyValue result = reader.read();

        // then
        assertNull(result);

        // verify
        verify(cursor).hasNext();
        verify(cursor).next();
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("Reader - 더 이상 읽을 데이터가 없는 경우 테스트")
    void noMoreDataTest() throws Exception {
        // given
        given(cursor.hasNext()).willReturn(false);

        // when
        KeyValue result = reader.read();

        // then
        assertNull(result);

        // verify
        verify(cursor).hasNext();
        verify(cursor, never()).next();
        verify(valueOperations, never()).get(any());
    }

    @Test
    @DisplayName("Processor - 키/값을 ChatMessageStatus로 정상 변환하는지 테스트")
    void processorTest() throws Exception {
        // given
        KeyValue item = new KeyValue("chat:last_read:1:2", "100");

        // when
        ChatMessageStatus result = processor.process(item);

        // then
        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals(1L, result.getChatRoomId());
        assertEquals(100L, result.getLastReadMessageId());
    }

    @Test
    @DisplayName("Processor - 잘못된 형식의 키는 null을 반환하는지 테스트")
    void processorInvalidKeyTest() throws Exception {
        // given
        KeyValue item = new KeyValue("invalid:key:format", "100");

        // when
        ChatMessageStatus result = processor.process(item);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("Writer - 데이터를 정상적으로 저장하는지 테스트")
    void writerTest() throws Exception {
        // given
        List<ChatMessageStatus> items = List.of(
                new ChatMessageStatus(1L, 1L, 100L),
                new ChatMessageStatus(1L, 2L, 200L),
                new ChatMessageStatus(2L, 1L, 300L)
        );
        Chunk<ChatMessageStatus> chunk = new Chunk<>(items);

        // when
        writer.write(chunk);

        // then
        verify(repository).saveLastReadMessageIdInBulk(1L, 1L, 100L);
        verify(repository).saveLastReadMessageIdInBulk(1L, 2L, 200L);
        verify(repository).saveLastReadMessageIdInBulk(2L, 1L, 300L);
    }
}
