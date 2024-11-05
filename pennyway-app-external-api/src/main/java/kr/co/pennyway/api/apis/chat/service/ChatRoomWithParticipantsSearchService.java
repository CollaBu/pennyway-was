package kr.co.pennyway.api.apis.chat.service;

import kr.co.pennyway.api.apis.chat.dto.ChatRoomRes;
import kr.co.pennyway.api.apis.chat.mapper.ChatRoomMapper;
import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import kr.co.pennyway.domain.common.redis.message.service.ChatMessageService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomWithParticipantsSearchService {
    private static final int MESSAGE_LIMIT = 15;

    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;

    @Transactional(readOnly = true)
    public ChatRoomRes.RoomWithParticipants execute(Long userId, Long chatRoomId) {
        // 내 정보 조회
        ChatMember myInfo = chatMemberService.readChatMember(userId, chatRoomId)
                .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));
        ChatMemberResult.Detail myDetail = new ChatMemberResult.Detail(myInfo.getId(), myInfo.getName(), myInfo.getRole(), myInfo.isNotifyEnabled(), userId, myInfo.getCreatedAt());

        // 최근 메시지 조회 (15건)
        List<ChatMessage> chatMessages = chatMessageService.readRecentMessages(chatRoomId, MESSAGE_LIMIT);

        // 최근 메시지의 발신자 조회
        Set<Long> recentParticipantIds = chatMessages.stream()
                .map(ChatMessage::getSender)
                .filter(sender -> !sender.equals(userId))
                .collect(Collectors.toSet());

        // 최근 메시지의 발신자 상세 정보 조회
        List<ChatMemberResult.Detail> recentParticipants = new ArrayList<>(
                chatMemberService.readChatMembersByUserIdIn(chatRoomId, recentParticipantIds)
        );

        // 내가 관리자가 아니거나, 최근 활동자에 관리자가 없다면 관리자 정보 조회
        if (!myInfo.getRole().equals(ChatMemberRole.ADMIN) && recentParticipants.stream().noneMatch(participant -> participant.role().equals(ChatMemberRole.ADMIN))) {
            ChatMemberResult.Detail admin = chatMemberService.readAdmin(chatRoomId)
                    .orElseThrow(() -> new ChatMemberErrorException(ChatMemberErrorCode.NOT_FOUND));
            recentParticipantIds.add(admin.userId());
            recentParticipants.add(admin);
        }
        recentParticipantIds.add(userId);

        // 채팅방에 속한 다른 사용자 요약 정보 조회
        List<ChatMemberResult.Summary> otherMemberIds = chatMemberService.readChatMemberIdsByUserIdNotIn(chatRoomId, recentParticipantIds);

        return ChatRoomMapper.toChatRoomResRoomWithParticipants(myDetail, recentParticipants, otherMemberIds, chatMessages);
    }
}
