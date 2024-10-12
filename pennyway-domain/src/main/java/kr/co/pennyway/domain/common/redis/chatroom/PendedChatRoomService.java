package kr.co.pennyway.domain.common.redis.chatroom;

import kr.co.pennyway.common.annotation.DomainService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DomainService
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PendedChatRoomService {
    private final PendedChatRoomRepository pendedChatRoomRepository;

    public void save(PendedChatRoom pendedChatRoom) {
        pendedChatRoomRepository.save(pendedChatRoom);
    }

    public PendedChatRoom read(Long id) {
        return pendedChatRoomRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 채팅방입니다.")
        );
    }
}
