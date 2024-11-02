package kr.co.pennyway.domain.common.redis.message.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public ChatMessage save(ChatMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            String chatRoomKey = getChatRoomKey(message.getChatRoomId());

            redisTemplate.opsForZSet().add( // ZADD
                    chatRoomKey,
                    messageJson,
                    message.getChatId()
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to save chat message: {}", message, e);
            throw new RuntimeException("Failed to save chat message", e);
        }

        return message;
    }

    @Override
    public List<ChatMessage> findRecentMessages(Long roomId, int limit) {
        String chatRoomKey = getChatRoomKey(roomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRange( // ZREVRANGE
                chatRoomKey,
                0,
                limit - 1
        );

        return convertToMessages(messageJsonSet);
    }

    @Override
    public SliceImpl<ChatMessage> findMessagesBefore(Long roomId, Long lastMessageId, int size) {
        String chatRoomKey = getChatRoomKey(roomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRangeByScore( // ZREVRANGEBYSCORE
                chatRoomKey,
                0, // 최소값
                lastMessageId - 1, // 마지막으로 조회한 메시지 이전까지
                0, // offset
                size + 1 // size + 1 만큼 조회하여 다음 페이지 존재 여부 확인
        );

        List<ChatMessage> messages = convertToMessages(messageJsonSet);
        boolean hasNext = messages.size() > size;

        if (hasNext) {
            messages = messages.subList(0, size);
        }

        return new SliceImpl<>(messages, PageRequest.of(0, size), hasNext);
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
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatMessage.class);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse chat message JSON: {}", json, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public Long countUnreadMessages(Long roomId, Long lastReadMessageId) {
        String chatRoomKey = getChatRoomKey(roomId);

        return redisTemplate.opsForZSet().count( // ZCOUNT
                chatRoomKey,
                lastReadMessageId + 1,  // 최소값 (lastReadMessageId 이후)
                Double.POSITIVE_INFINITY  // 최대값
        );
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
}
