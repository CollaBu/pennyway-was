package kr.co.pennyway.domain.domains.chatstatus.repository;

import java.util.Optional;

public interface ChatMessageStatusCacheRepository {
    /**
     * 캐시 데이터에서 마지막으로 읽은 메시지 ID를 조회합니다.
     */
    Optional<Long> findLastReadMessageId(Long userId, Long chatRoomId);

    /**
     * 캐시 데이터에 마지막으로 읽은 메시지 ID를 저장합니다.
     */
    void saveLastReadMessageId(Long userId, Long chatRoomId, Long messageId);

    /**
     * 캐시 데이터를 삭제합니다.
     */
    void deleteLastReadMessageId(Long userId, Long chatRoomId);
}
