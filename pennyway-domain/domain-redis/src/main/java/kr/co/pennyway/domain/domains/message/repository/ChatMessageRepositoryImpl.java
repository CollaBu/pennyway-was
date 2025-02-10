package kr.co.pennyway.domain.domains.message.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {
    private static final int COUNTER_DIGITS = 4;
    private static final String SEPARATOR = "|";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public ChatMessage save(ChatMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            String chatRoomKey = getChatRoomKey(message.getChatRoomId());

            String tsidKey = formatTsidKey(message.getChatId());

            redisTemplate.opsForZSet().add(chatRoomKey, tsidKey + SEPARATOR + messageJson, 0);
        } catch (JsonProcessingException e) {
            log.error("Failed to save chat message: {}", message, e);
            throw new RuntimeException("Failed to save chat message", e);
        }

        return message;
    }

    @Override
    public List<ChatMessage> findRecentMessages(Long roomId, int limit) {
        String chatRoomKey = getChatRoomKey(roomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRangeByLex(chatRoomKey, Range.unbounded(), Limit.limit().count(limit));

        return convertToMessages(messageJsonSet);
    }

    @Override
    public SliceImpl<ChatMessage> findMessagesBefore(Long roomId, Long lastMessageId, int size) {
        String chatRoomKey = getChatRoomKey(roomId);
        String tsidKey = formatTsidKey(lastMessageId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRangeByLex(
                chatRoomKey,
                Range.of(Range.Bound.unbounded(), Range.Bound.exclusive(tsidKey)),
                Limit.limit().count(size + 1)
        );
        List<ChatMessage> messages = convertToMessages(messageJsonSet);

        boolean hasNext = messages.size() > size;

        if (hasNext) {
            messages = messages.subList(0, size);
        }

        return new SliceImpl<>(messages, PageRequest.of(0, size), hasNext);
    }

    @Override
    public Long countUnreadMessages(Long roomId, Long lastReadMessageId) {
        if (lastReadMessageId == null || lastReadMessageId < 0) {
            throw new IllegalArgumentException("lastReadMessageId must not be null");
        }

        if (lastReadMessageId == 0L) {
            return redisTemplate.opsForZSet().zCard(getChatRoomKey(roomId));
        }

        String chatRoomKey = getChatRoomKey(roomId);
        String tsidKey = formatTsidKey(lastReadMessageId);

        Long totalCount = redisTemplate.opsForZSet().lexCount(chatRoomKey, Range.of(Range.Bound.inclusive(tsidKey), Range.Bound.unbounded()));

        return totalCount > 0 ? totalCount - 1 : 0;
    }

    /**
     * JSON 문자열 집합을 ChatMessage 객체 리스트로 변환합니다.
     * 변환 실패한 메시지는 무시됩니다.
     *
     * @param messageJsonSet JSON 문자열 집합
     * @return 변환된 ChatMessage 객체 리스트
     */
    private List<ChatMessage> convertToMessages(Set<String> messageJsonSet) {
        if (messageJsonSet == null || messageJsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        return messageJsonSet.stream()
                .map(value -> {
                    try {
                        String json = value.substring(value.indexOf(SEPARATOR) + 1);
                        return objectMapper.readValue(json, ChatMessage.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse chat message JSON: {}", value, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 채팅방의 조회용 Redis key를 생성합니다.
     *
     * @param roomId 채팅방 ID
     * @return 생성된 Redis key
     */
    private String getChatRoomKey(Long roomId) {
        return "chatroom:" + roomId + ":message";
    }

    /**
     * TSID를 lexicographical sorting이 가능한 형태의 문자열로 변환
     * format: {timestamp부분:16진수}:{counter부분:4자리}
     */
    private String formatTsidKey(long tsid) {
        String tsidStr = String.valueOf(tsid);

        String timestamp = tsidStr.substring(0, tsidStr.length() - COUNTER_DIGITS);
        String counter = tsidStr.substring(tsidStr.length() - COUNTER_DIGITS);

        return timestamp + ":" + counter;
    }
}
