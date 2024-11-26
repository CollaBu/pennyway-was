package kr.co.pennyway.socket.service;

import kr.co.pennyway.domain.context.chat.service.ChatMessageStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LastMessageIdSaveService {
    private final ChatMessageStatusService chatMessageStatusService;

    public void execute(Long userId, Long chatRoomId, Long lastReadMessageId) {
        chatMessageStatusService.saveLastReadMessageId(userId, chatRoomId, lastReadMessageId);
    }
}
