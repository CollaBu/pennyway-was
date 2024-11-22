package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageStatusRedisService {
    private final ChatMessageStatusCacheRepository chatMessageStatusCacheRepository;

    public Optional<Long> findLastReadMessageId(Long userId, Long chatRoomId) {
        return chatMessageStatusCacheRepository.findLastReadMessageId(userId, chatRoomId);
    }

    public void saveLastReadMessageId(Long userId, Long chatRoomId, Long messageId) {
        chatMessageStatusCacheRepository.saveLastReadMessageId(userId, chatRoomId, messageId);
    }

    public void deleteLastReadMessageId(Long userId, Long chatRoomId) {
        chatMessageStatusCacheRepository.deleteLastReadMessageId(userId, chatRoomId);
    }
}
