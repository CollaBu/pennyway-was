package kr.co.pennyway.domain.domains.message.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import kr.co.pennyway.domain.domains.message.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;

import java.util.List;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageRedisService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage create(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * 채팅방의 최근 메시지를 조회합니다.
     *
     * @param roomId Long: 채팅방 ID
     * @param limit  int: 조회할 메시지 개수
     * @return 최근 시간 순으로 정렬된 최근 메시지 목록
     */
    public List<ChatMessage> readRecentMessages(Long roomId, int limit) {
        return chatMessageRepository.findRecentMessages(roomId, limit);
    }

    /**
     * 특정 메시지 ID 이전의 메시지들을 페이징하여 조회합니다.
     * 최근 시간 기준으로 정렬된 결과를 반환하며, lastMessageId에 해당하는 메시지는 포함되지 않습니다.
     * 만약, lastMessageId에 해당하는 메시지가 필요한 경우 인자는 lastMessageId + 1로 설정해야 합니다.
     *
     * @param roomId        Long: 채팅방 ID
     * @param lastMessageId Long: 마지막으로 조회한 메시지의 TSID
     * @param size          int: 조회할 메시지 개수
     * @return 페이징된 메시지 목록
     */
    public Slice<ChatMessage> readMessagesBefore(Long roomId, Long lastMessageId, int size) {
        return chatMessageRepository.findMessagesBefore(roomId, lastMessageId, size);
    }

    /**
     * 사용자가 마지막으로 읽은 메시지 이후의 안 읽은 메시지 개수를 조회합니다.
     *
     * @param roomId            채팅방 ID
     * @param lastReadMessageId 사용자가 마지막으로 읽은 메시지의 TSID
     * @return 안 읽은 메시지 개수
     */
    public Long countUnreadMessages(Long roomId, Long lastReadMessageId) {
        return chatMessageRepository.countUnreadMessages(roomId, lastReadMessageId);
    }
}
