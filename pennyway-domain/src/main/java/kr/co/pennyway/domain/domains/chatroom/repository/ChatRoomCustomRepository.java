package kr.co.pennyway.domain.domains.chatroom.repository;

import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;

import java.util.List;

public interface ChatRoomCustomRepository {
    /**
     * 사용자가 참여한 채팅방 목록을 조회하며, 응답은 List<{@link ChatRoomDetail}> 형태로 반환한다.
     * 반환된 채팅방 목록은 정렬 순서를 보장하지 않는다.
     */
    List<ChatRoomDetail> findChatRoomsByUserId(Long userId);
}
