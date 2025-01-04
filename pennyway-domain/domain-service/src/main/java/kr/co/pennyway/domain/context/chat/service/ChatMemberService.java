package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberRdbService chatMemberRdbService;

    @Transactional
    public ChatMember createAdmin(User user, ChatRoom chatRoom) {
        return chatMemberRdbService.createAdmin(user, chatRoom);
    }

    @Transactional
    public ChatMember createMember(User user, ChatRoom chatRoom) {
        return chatMemberRdbService.createMember(user, chatRoom);
    }

    @Transactional(readOnly = true)
    public Optional<ChatMemberResult.Detail> readAdmin(Long chatRoomId) {
        return chatMemberRdbService.readAdmin(chatRoomId);
    }

    @Transactional(readOnly = true)
    public Optional<ChatMember> readChatMember(Long userId, Long chatMemberId) {
        return chatMemberRdbService.readChatMember(userId, chatMemberId);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResult.Detail> readChatMembersByMemberIds(Long chatRoomId, Set<Long> chatMemberIds) {
        return chatMemberRdbService.readChatMembersByIdIn(chatRoomId, chatMemberIds);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResult.Detail> readChatMembersByUserIds(Long chatRoomId, Set<Long> userIds) {
        return chatMemberRdbService.readChatMembersByUserIdIn(chatRoomId, userIds);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResult.Summary> readChatMemberIdsByUserIdsNotIn(Long chatRoomId, Set<Long> userIds) {
        return chatMemberRdbService.readChatMemberIdsByUserIdNotIn(chatRoomId, userIds);
    }

    @Transactional(readOnly = true)
    public Set<Long> readChatRoomIdsByUserId(Long userId) {
        return chatMemberRdbService.readChatRoomIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Set<Long> readUserIdsByChatRoomId(Long chatRoomId) {
        return chatMemberRdbService.readUserIdsByChatRoomId(chatRoomId);
    }

    /**
     * 채팅방에 해당 유저가 존재하는지 확인한다.
     * 이 때, 삭제된 사용자 데이터는 조회하지 않는다.
     */
    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId) {
        return chatMemberRdbService.isExists(chatRoomId, userId);
    }

    /**
     * 삭제된 사용자 데이터는 조회하지 않는다.
     */
    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId, Long chatMemberId) {
        return chatMemberRdbService.isExists(chatRoomId, userId, chatMemberId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserChatRoomOwnership(Long userId) {
        return chatMemberRdbService.hasUserChatRoomOwnership(userId);
    }

    @Transactional(readOnly = true)
    public long countActiveMembers(Long chatRoomId) {
        return chatMemberRdbService.countActiveMembers(chatRoomId);
    }
}
