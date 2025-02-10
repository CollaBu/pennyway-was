package kr.co.pennyway.domain.domains.member.repository;

import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;

import java.util.Optional;

public interface CustomChatMemberRepository {
    /**
     * 채팅방에 해당 유저가 존재하는지 확인한다.
     * 이 때, 삭제된 사용자 데이터는 조회하지 않는다.
     */
    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    /**
     * 해당 유저가 채팅방장으로 가입한 채팅방이 존재하는지 확인한다.
     */
    boolean existsOwnershipChatRoomByUserId(Long userId);

    boolean existsByChatRoomIdAndUserIdAndId(Long chatRoomId, Long userId, Long chatMemberId);

    /**
     * 채팅방의 관리자 정보를 조회한다.
     */
    Optional<ChatMemberResult.Detail> findAdminByChatRoomId(Long chatRoomId);
}
