package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatstatus.service.ChatMessageStatusRdbService;
import kr.co.pennyway.domain.domains.chatstatus.service.ChatMessageStatusRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageStatusService {
    private final ChatMessageStatusRdbService rdbService;
    private final ChatMessageStatusRedisService redisService;

    /**
     * 마지막으로 읽은 메시지 ID를 저장합니다.
     *
     * @throws IllegalArgumentException 사용자 ID, 채팅방 ID, 메시지 ID가 null이거나 0보다 작을 경우
     */
    public void saveLastReadMessageId(Long userId, Long roomId, Long messageId) {
        validateInputs(userId, roomId, messageId);
        redisService.saveLastReadMessageId(userId, roomId, messageId);
    }

    /**
     * 마지막으로 읽은 메시지 ID를 조회합니다.
     *
     * @return 마지막으로 읽은 메시지 ID가 없을 경우 0을 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long readLastReadMessageId(Long userId, Long chatRoomId) {
        return redisService.readLastReadMessageId(userId, chatRoomId)
                .orElseGet(() -> rdbService
                        .readByUserIdAndChatRoomId(userId, chatRoomId)
                        .map(status -> {
                            Long lastReadId = status.getLastReadMessageId();
                            redisService.saveLastReadMessageId(userId, chatRoomId, lastReadId);
                            return lastReadId;
                        })
                        .orElse(0L));
    }

    private void validateInputs(Long userId, Long chatRoomId, Long lastReadMessageId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        if (chatRoomId == null || chatRoomId <= 0) {
            throw new IllegalArgumentException("Invalid chatRoomId: " + chatRoomId);
        }
        if (lastReadMessageId == null || lastReadMessageId <= 0) {
            throw new IllegalArgumentException("Invalid lastReadMessageId: " + lastReadMessageId);
        }
    }
}
