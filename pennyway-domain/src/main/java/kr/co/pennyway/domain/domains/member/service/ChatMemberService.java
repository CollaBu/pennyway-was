package kr.co.pennyway.domain.domains.member.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;

    @Transactional
    public void create(ChatMember chatMember) {
        chatMemberRepository.save(chatMember);
    }

    @Transactional(readOnly = true)
    public boolean isExists(Long chatRoomId, Long userId) {
        return chatMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }
}
