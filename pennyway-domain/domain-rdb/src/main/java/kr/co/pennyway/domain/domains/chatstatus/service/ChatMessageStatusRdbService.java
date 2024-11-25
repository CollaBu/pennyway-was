package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageStatusRdbService {
    private final ChatMessageStatusRepository chatMessageStatusRepository;

    @Transactional(readOnly = true)
    public Optional<ChatMessageStatus> readByUserIdAndChatRoomId(Long userId, Long chatRoomId) {
        return chatMessageStatusRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
    }
}
