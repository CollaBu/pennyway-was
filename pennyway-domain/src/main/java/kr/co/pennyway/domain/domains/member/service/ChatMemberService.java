package kr.co.pennyway.domain.domains.member.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;

    @Transactional
    public ChatMember createAdmin(String nickname, User user, ChatRoom chatRoom) {
        ChatMember chatMember = ChatMember.of(nickname, user, chatRoom, ChatMemberRole.ADMIN);

        return chatMemberRepository.save(chatMember);
    }

    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId) {
        return chatMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }
}
