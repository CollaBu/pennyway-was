package kr.co.pennyway.domain.domains.chatroom.repository;

import kr.co.pennyway.domain.domains.chatroom.dto.ChatRoomDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ChatRoomCustomRepository {
    /**
     * 사용자가 참여한 채팅방 목록을 조회하며, 응답은 List<{@link ChatRoomDetail}> 형태로 반환한다.
     * 반환된 채팅방 목록은 정렬 순서를 보장하지 않는다.
     */
    List<ChatRoomDetail> findChatRoomsByUserId(Long userId);

    /**
     * target 파라미터로 채팅방 제목, 설명과 일치하는 항목을 탐색하여 결과를 반환한다.
     * 응답은 Slice<{@link ChatRoomDetail}> 형태로 반환되며, 반환된 채팅방 목록은 가장 매칭 점수가 높은 순서대로 정렬된다.
     *
     * @param target   검색 대상
     * @param pageable 페이징 정보
     * @return 채팅방 목록
     */
    Slice<ChatRoomDetail> findChatRooms(String target, Pageable pageable);
}
