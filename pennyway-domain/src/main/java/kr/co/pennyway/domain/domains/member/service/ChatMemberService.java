package kr.co.pennyway.domain.domains.member.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;

    @Transactional
    public ChatMember createAdmin(User user, ChatRoom chatRoom) {
        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN);

        return chatMemberRepository.save(chatMember);
    }

    @Transactional
    public ChatMember createMember(User user, ChatRoom chatRoom) {
        Set<ChatMember> chatMembers = chatMemberRepository.findByChat_Room_IdAndUser_Id(chatRoom.getId(), user.getId());

        if (chatMembers.stream().anyMatch(ChatMember::isActive)) {
            log.warn("사용자는 이미 채팅방에 가입되어 있습니다. chatRoomId: {}, userId: {}", chatRoom.getId(), user.getId());
            throw new ChatMemberErrorException(ChatMemberErrorCode.ALREADY_JOINED);
        }

        if (chatMembers.stream().anyMatch(ChatMember::isBanned)) {
            log.warn("사용자는 채팅방에서 추방된 이력이 존재합니다. chatRoomId: {}, userId: {}", chatRoom.getId(), user.getId());
            throw new ChatMemberErrorException(ChatMemberErrorCode.BANNED);
        }

        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);

        return chatMemberRepository.save(chatMember);
    }

    /**
     * 채팅방에 해당 유저가 존재하는지 확인한다.
     * 이 때, 삭제된 사용자 데이터는 조회하지 않는다.
     */
    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId) {
        return chatMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }

    @Transactional(readOnly = true)
    public long countActiveMembers(Long chatRoomId) {
        return chatMemberRepository.countByChatRoomIdAndActive(chatRoomId);
    }
}
