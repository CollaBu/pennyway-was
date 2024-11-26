package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatMapper;
import kr.co.pennyway.api.apis.chat.service.ChatSearchService;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ChatUseCase {
    private final ChatSearchService chatSearchService;

    public SliceResponseTemplate<ChatRes.ChatDetail> readChats(Long chatRoomId, Long lastMessageId, int size) {
        Slice<ChatMessage> chatMessages = chatSearchService.readChats(chatRoomId, lastMessageId, size);

        return ChatMapper.toChatDetails(chatMessages);
    }
}
