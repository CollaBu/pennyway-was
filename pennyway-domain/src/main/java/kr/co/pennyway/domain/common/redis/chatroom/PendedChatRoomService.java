package kr.co.pennyway.domain.common.redis.chatroom;

import kr.co.pennyway.common.annotation.DomainService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PendedChatRoomService {
    private final PendedChatRoomRepository pendedChatRoomRepository;

    public PendedChatRoom create(PendedChatRoom pendedChatRoom) {
        return pendedChatRoomRepository.save(pendedChatRoom);
    }

    public Optional<PendedChatRoom> readByUserId(Long userId) {
        return pendedChatRoomRepository.findById(userId);
    }
}
