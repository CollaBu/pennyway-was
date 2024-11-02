package kr.co.pennyway.domain.common.redis.message.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public interface ChatMessageRepository {
    /**
     * 채팅 메시지를 Redis에 저장합니다.
     * 메시지는 JSON 형태로 직렬화되어 저장되며, TSID를 score로 사용하여 정렬됩니다.
     *
     * @param message {@link ChatMessage}: 저장할 채팅 메시지
     * @throws JsonProcessingException JSON 직렬화에 실패한 경우
     */
    ChatMessage save(ChatMessage message);

    /**
     * 채팅방의 최근 메시지를 조회합니다.
     * 메시지는 시간 순으로 정렬되어 반환됩니다.
     *
     * @param roomId Long: 채팅방 ID
     * @param limit  int: 조회할 메시지 개수
     * @return 최근 메시지 목록
     */
    List<ChatMessage> findRecentMessages(Long roomId, int limit);

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
    SliceImpl<ChatMessage> findMessagesBefore(Long roomId, Long lastMessageId, int size);

    /**
     * 사용자가 마지막으로 읽은 메시지 이후의 안 읽은 메시지 개수를 조회합니다.
     *
     * @param roomId            채팅방 ID
     * @param lastReadMessageId 사용자가 마지막으로 읽은 메시지의 TSID
     * @return 안 읽은 메시지 개수
     */
    Long countUnreadMessages(Long roomId, Long lastReadMessageId);
}
