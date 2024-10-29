package kr.co.pennyway.domain.domains.member.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface ChatMemberRepository extends ExtendedRepository<ChatMember, Long>, CustomChatMemberRepository {
    @Transactional(readOnly = true)
    Set<ChatMember> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    @Transactional(readOnly = true)
    @Query("SELECT COUNT(*) FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId AND cm.deletedAt IS NULL")
    long countByChatRoomIdAndActive(Long chatRoomId);
}
