package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemberSearchService {
    private final ChatMemberService chatMemberService;

    public Set<Long> readJoinedChatRoomIds(Long userId) {
        return chatMemberService.readChatRoomIdsByUserId(userId);
    }

    public List<ChatMemberResult.Detail> readChatMembers(Long chatRoomId, Set<Long> chatMemberIds) {
        return chatMemberService.readChatMembersByMemberIds(chatRoomId, chatMemberIds);
    }
}
