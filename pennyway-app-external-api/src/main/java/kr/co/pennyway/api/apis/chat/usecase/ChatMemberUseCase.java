package kr.co.pennyway.api.apis.chat.usecase;

import kr.co.pennyway.api.apis.chat.dto.ChatMemberRes;
import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatMemberMapper;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.api.apis.chat.service.ChatMemberJoinService;
import kr.co.pennyway.api.apis.chat.service.ChatMemberSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ChatMemberUseCase {
    private final ChatMemberJoinService chatMemberJoinService;
    private final ChatMemberSearchService chatMemberSearchService;

    public ChatRoomRes.Detail joinChatRoom(Long userId, Long chatRoomId, Integer password) {
        Triple<ChatRoom, Integer, Long> chatRoom = chatMemberJoinService.execute(userId, chatRoomId, password);

        return ChatRoomMapper.toChatRoomResDetail(chatRoom.getLeft(), false, chatRoom.getMiddle(), chatRoom.getRight());
    }

    public List<ChatMemberRes.MemberDetail> readChatMembers(Long chatRoomId, Set<Long> chatMemberIds) {
        List<ChatMemberResult.Detail> chatMembers = chatMemberSearchService.readChatMembers(chatRoomId, chatMemberIds);

        return ChatMemberMapper.toChatMemberResDetail(chatMembers);
    }
}
