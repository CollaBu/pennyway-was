package kr.co.pennyway.domain.domains.chatstatus.repository;

import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatMessageStatusRepository extends JpaRepository<ChatMessageStatus, Long> {
    Optional<ChatMessageStatus> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT c FROM ChatMessageStatus c WHERE c.userId = :userId AND c.chatRoomId IN :roomIds")
    List<ChatMessageStatus> findAllByUserIdAndChatRoomIdIn(Long userId, Collection<Long> roomIds);

    @Modifying
    @Query(value = """
            INSERT INTO chat_message_status (user_id, chat_room_id, last_read_message_id, updated_at)
            VALUES (:userId, :roomId, :messageId, NOW())
            ON DUPLICATE KEY UPDATE
            last_read_message_id = GREATEST(last_read_message_id, :messageId),
            updated_at = NOW()
            """, nativeQuery = true)
    void saveLastReadMessageIdInBulk(Long userId, Long roomId, Long messageId);
}