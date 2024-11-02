package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
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

    public List<ChatMember> readChatMembers(Long chatRoomId, Set<Long> memberIds) {
        return chatMemberService.readChatMembersByMemberIdIn(chatRoomId, memberIds);
    }
}
