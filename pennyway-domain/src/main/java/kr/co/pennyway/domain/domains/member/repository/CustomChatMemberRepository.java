package kr.co.pennyway.domain.domains.member.repository;

public interface CustomChatMemberRepository {
    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
