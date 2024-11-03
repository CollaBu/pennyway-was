package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageStatusService {
    private final ChatMessageStatusRepository chatMessageStatusRepository;

    /**
     * 마지막으로 읽은 메시지 ID를 저장합니다.
     */
    public void saveLastReadMessageId(Long userId, Long roomId, Long messageId) {
        chatMessageStatusRepository.saveLastReadMessageId(userId, roomId, messageId);
    }

    /**
     * 마지막으로 읽은 메시지 ID를 조회합니다.
     *
     * @return 마지막으로 읽은 메시지 ID가 없을 경우 0을 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long readLastReadMessageId(Long userId, Long chatRoomId) {
        return chatMessageStatusRepository.findLastReadMessageId(userId, chatRoomId)
                .orElseGet(() -> chatMessageStatusRepository
                        .findByUserIdAndChatRoomId(userId, chatRoomId)
                        .map(status -> {
                            Long lastReadId = status.getLastReadMessageId();
                            chatMessageStatusRepository.saveLastReadMessageId(userId, chatRoomId, lastReadId);
                            return lastReadId;
                        })
                        .orElse(0L));
    }

    /**
     * 여러 사용자의 읽은 메시지 ID를 일괄 업데이트합니다.
     *
     * @param updates 사용자별 읽은 메시지 ID 목록 (사용자 ID -> (채팅방 ID -> 메시지 ID))
     */
    @Transactional
    public void bulkUpdateReadStatus(Map<Long, Map<Long, Long>> updates) {
        updates.forEach((userId, roomUpdates) ->
                roomUpdates.forEach((roomId, messageId) -> {
                    chatMessageStatusRepository.saveLastReadMessageId(userId, roomId, messageId);
                    chatMessageStatusRepository.deleteLastReadMessageId(userId, roomId);
                })
        );
    }
}
