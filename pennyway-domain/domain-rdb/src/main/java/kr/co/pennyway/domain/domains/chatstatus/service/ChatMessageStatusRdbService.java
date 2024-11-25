package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageStatusRdbService {
    private final ChatMessageStatusRepository chatMessageStatusRepository;

    @Transactional(readOnly = true)
    public Optional<ChatMessageStatus> readByUserIdAndChatRoomId(Long userId, Long chatRoomId) {
        return chatMessageStatusRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
    }

    /**
     * 여러 사용자의 읽은 메시지 ID를 일괄 업데이트합니다.
     *
     * @param updates 사용자별 읽은 메시지 ID 목록 (사용자 ID -> (채팅방 ID -> 메시지 ID))
     */
    // TODO: 이 메서드는 임시로 사용되는 메서드이며, 성능 평가 이후 개선될 여지가 있습니다.
    @Transactional
    public void bulkUpdateReadStatus(Map<Long, Map<Long, Long>> updates) {
        updates.forEach((userId, roomUpdates) ->
                roomUpdates.forEach((roomId, messageId) -> {
                    chatMessageStatusRepository.saveLastReadMessageIdInBulk(userId, roomId, messageId);
                })
        );
    }
}
