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
public class ChatMessageRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 채팅 메시지를 Redis에 저장합니다.
     * 메시지는 JSON 형태로 직렬화되어 저장되며, TSID를 score로 사용하여 정렬됩니다.
     *
     * @param message {@link ChatMessage}: 저장할 채팅 메시지
     * @throws JsonProcessingException JSON 직렬화에 실패한 경우
     */
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
//            throw new RedisOperationException("Failed to save chat message", e);
        }

        return message;
    }

    /**
     * 채팅방의 최근 메시지를 조회합니다.
     * 메시지는 시간 순으로 정렬되어 반환됩니다.
     *
     * @param roomId Long: 채팅방 ID
     * @param limit  int: 조회할 메시지 개수
     * @return 최근 메시지 목록
     */
    public List<ChatMessage> findRecentMessages(Long roomId, int limit) {
        String chatRoomKey = getChatRoomKey(roomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRange( // ZREVRANGE
                chatRoomKey,
                0,
                limit - 1
        );

        return convertToMessages(messageJsonSet);
    }

    /**
     * 특정 메시지 ID 이전의 메시지들을 페이징하여 조회합니다.
     * TSID를 기준으로 정렬된 결과를 반환하며, lastMessageId에 해당하는 메시지는 포함되지 않습니다.
     * 만약, lastMessageId에 해당하는 메시지가 필요한 경우 인자는 lastMessageId + 1로 설정해야 합니다.
     *
     * @param roomId        Long: 채팅방 ID
     * @param lastMessageId Long: 마지막으로 조회한 메시지의 TSID
     * @param size          int: 조회할 메시지 개수
     * @return 페이징된 메시지 목록과 다음 페이지 존재 여부
     */
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

    /**
     * 사용자가 마지막으로 읽은 메시지 이후의 안 읽은 메시지 개수를 조회합니다.
     *
     * @param roomId            채팅방 ID
     * @param lastReadMessageId 사용자가 마지막으로 읽은 메시지의 TSID
     * @return 안 읽은 메시지 개수
     */
    public Long countUnreadMessages(Long roomId, Long lastReadMessageId) {
        String chatRoomKey = getChatRoomKey(roomId);

        // lastReadMessageId보다 큰 score를 가진 메시지의 개수를 조회
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
