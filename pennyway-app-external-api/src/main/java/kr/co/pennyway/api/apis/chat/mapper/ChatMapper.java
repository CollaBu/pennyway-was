package kr.co.pennyway.api.apis.chat.mapper;

import kr.co.pennyway.api.apis.chat.dto.ChatRes;
import kr.co.pennyway.api.common.response.SliceResponseTemplate;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.message.domain.ChatMessage;
import org.springframework.data.domain.Slice;

import java.util.List;

@Mapper
public class ChatMapper {
    public static SliceResponseTemplate<ChatRes.ChatDetail> toChatDetails(Slice<ChatMessage> chatMessages) {
        List<ChatRes.ChatDetail> details = chatMessages.getContent().stream()
                .map(ChatRes.ChatDetail::from)
                .toList();

        return SliceResponseTemplate.of(details, chatMessages.getPageable(), chatMessages.getNumberOfElements(), chatMessages.hasNext());
    }
}
