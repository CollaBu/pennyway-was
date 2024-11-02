package kr.co.pennyway.domain.domains.member.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatMemberRepository extends ExtendedRepository<ChatMember, Long>, CustomChatMemberRepository {
    @Transactional(readOnly = true)
    Set<ChatMember> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    @Transactional(readOnly = true)
    @Query("SELECT cm FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId AND cm.user.id = :userId AND cm.deletedAt IS NULL")
    Optional<ChatMember> findActiveChatMember(Long chatRoomId, Long userId);

    @Transactional(readOnly = true)
    @Query("SELECT cm FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId AND cm.user.id IN :memberIds AND cm.deletedAt IS NULL")
    List<ChatMember> findByChatRoom_IdAndUser_IdIn(Long chatRoomId, Set<Long> memberIds);

    @Transactional(readOnly = true)
    @Query("SELECT cm.user.id FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId AND cm.user.id NOT IN :memberIds AND cm.deletedAt IS NULL")
    List<Long> findByChatRoom_IdAndUser_IdNotIn(Long chatRoomId, Set<Long> memberIds);

    @Transactional(readOnly = true)
    @Query("SELECT COUNT(*) FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId AND cm.deletedAt IS NULL")
    long countByChatRoomIdAndActive(Long chatRoomId);

    @Transactional(readOnly = true)
    @Query("SELECT cm.chatRoom.id FROM ChatMember cm WHERE cm.user.id = :userId AND cm.deletedAt IS NULL")
    Set<Long> findChatRoomIdsByUserId(Long userId);
}
