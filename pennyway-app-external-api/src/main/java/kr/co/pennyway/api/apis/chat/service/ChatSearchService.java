package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSearchService {
    private final ChatMessageService chatMessageService;

    public Slice<ChatMessage> readChats(Long chatRoomId, Long lastMessageId, int size) {
        return chatMessageService.readMessagesBefore(chatRoomId, lastMessageId, size);
    }
}
