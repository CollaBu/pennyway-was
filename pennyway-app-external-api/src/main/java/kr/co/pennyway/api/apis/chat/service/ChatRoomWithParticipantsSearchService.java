package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomWithParticipantsSearchService {
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;

    @Transactional(readOnly = true)
    public ChatRoomRes.RoomWithParticipants execute(Long userId, Long chatRoomId) {
        // (1) myInfo: 내 정보 조회 - MySQL
        ChatMember myInfo = chatMemberService.readChatMember(userId, chatRoomId)
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));

        // (2) recentChats: 최근 채팅 조회 - Redis (key: `chatroom:{chatroom_id}:message:{message_id}`)
        List<ChatMessage> chatMessages = chatMessageService.readRecentMessages(chatRoomId, 15);

        // (3) recentParticipantIds: 최근 채팅 참여자 정보 상세 조회 - MySQL
        Set<Long> recentParticipantIds = chatMessages.stream()
                .map(ChatMessage::getSender)
                .filter(sender -> !sender.equals(userId))
                .collect(Collectors.toSet());

        List<ChatMember> recentParticipants = chatMemberService.readChatMembersByMemberIdIn(chatRoomId, recentParticipantIds);

        // (4) otherMemberIds: 나머지 멤버 ID 조회 - MySQL
        recentParticipantIds.add(userId);
        List<Long> otherMemberIds = chatMemberService.readChatMemberIdsByMemberIdNotIn(chatRoomId, recentParticipantIds);

        return ChatRoomMapper.toChatRoomResRoomWithParticipants(myInfo, recentParticipants, otherMemberIds, chatMessages);
    }
}
