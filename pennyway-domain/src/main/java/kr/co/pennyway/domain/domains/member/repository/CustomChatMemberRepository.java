package kr.co.pennyway.domain.domains.member.repository;

public interface CustomChatMemberRepository {
    /**
     * 채팅방에 해당 유저가 존재하는지 확인한다.
     * 이 때, 삭제된 사용자 데이터는 조회하지 않는다.
     */
    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
